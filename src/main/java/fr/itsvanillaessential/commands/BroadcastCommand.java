package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * /broadcast <message>
 * /broadcast --title <titre> [sous-titre]
 * /broadcast --sound <SOUND> <message>
 * /broadcast --world <monde> <message>
 * /broadcast --player <joueur> <message>
 * /broadcast --prefix <préfixe> <message>
 * /broadcast --center <message>   (center-padded)
 */
public class BroadcastCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public BroadcastCommand(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.broadcast")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        // ── /broadcast --title <titre> [sous-titre] ───────────────────────────
        if (args[0].equalsIgnoreCase("--title") || args[0].equalsIgnoreCase("-t")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --title <titre> [sous-titre]");
                return true;
            }
            String title    = CoreEssentials.colorize(args[1]);
            String subtitle = args.length > 2
                    ? CoreEssentials.colorize(String.join(" ", Arrays.copyOfRange(args, 2, args.length)))
                    : "";
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(title, subtitle, 10, 70, 20);
            }
            sender.sendMessage(plugin.prefix() + "§aTitre envoyé à tous les joueurs.");
            return true;
        }

        // ── /broadcast --sound <SOUND> <message> ─────────────────────────────
        if (args[0].equalsIgnoreCase("--sound") || args[0].equalsIgnoreCase("-s")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --sound <SOUND> <message>");
                return true;
            }
            Sound sound;
            try {
                sound = Sound.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(plugin.prefix() + "§cSon invalide: §e" + args[1]
                        + "§c. Ex: §fENTITY_PLAYER_LEVELUP");
                return true;
            }
            String message = buildMessage(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(message);
                p.playSound(p.getLocation(), sound, 1f, 1f);
            }
            sender.sendMessage(plugin.prefix() + "§aBroadcast avec son §e" + sound.name() + " §aenvoyé !");
            return true;
        }

        // ── /broadcast --world <monde> <message> ─────────────────────────────
        if (args[0].equalsIgnoreCase("--world") || args[0].equalsIgnoreCase("-w")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --world <monde> <message>");
                return true;
            }
            var world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(plugin.prefix() + "§cMonde §e" + args[1] + " §cintrouvable.");
                return true;
            }
            String message = buildMessage(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            world.getPlayers().forEach(p -> p.sendMessage(message));
            sender.sendMessage(plugin.prefix() + "§aBroadcast envoyé dans §e" + world.getName()
                    + " §a(" + world.getPlayers().size() + " joueur(s)).");
            return true;
        }

        // ── /broadcast --player <joueur> <message> ────────────────────────────
        if (args[0].equalsIgnoreCase("--player") || args[0].equalsIgnoreCase("-p")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --player <joueur> <message>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessages().playerNotFound(args[1]));
                return true;
            }
            String message = buildMessage(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            target.sendMessage(message);
            sender.sendMessage(plugin.prefix() + "§aMessage privé envoyé à §e" + target.getName() + "§a.");
            return true;
        }

        // ── /broadcast --prefix <préfixe> <message> ──────────────────────────
        if (args[0].equalsIgnoreCase("--prefix") || args[0].equalsIgnoreCase("-pre")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --prefix <préfixe> <message>");
                return true;
            }
            String customPrefix = CoreEssentials.colorize(args[1]);
            String rawMsg       = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String message      = customPrefix + " " + CoreEssentials.colorize(rawMsg);
            Bukkit.broadcastMessage(message);
            sender.sendMessage(plugin.prefix() + "§aBroadcast avec préfixe personnalisé envoyé !");
            return true;
        }

        // ── /broadcast --center <message> ─────────────────────────────────────
        if (args[0].equalsIgnoreCase("--center") || args[0].equalsIgnoreCase("-c")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --center <message>");
                return true;
            }
            String rawMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String message = centerText(CoreEssentials.colorize(rawMsg));
            Bukkit.broadcastMessage(message);
            sender.sendMessage(plugin.prefix() + "§aBroadcast centré envoyé !");
            return true;
        }

        // ── /broadcast --bar <message> (action bar) ───────────────────────────
        if (args[0].equalsIgnoreCase("--bar") || args[0].equalsIgnoreCase("-b")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.prefix() + "§cUsage: /broadcast --bar <message>");
                return true;
            }
            String rawMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            net.kyori.adventure.text.Component component =
                    net.kyori.adventure.text.Component.text(CoreEssentials.colorize(rawMsg));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(component);
            }
            sender.sendMessage(plugin.prefix() + "§aAction bar envoyée à tous les joueurs !");
            return true;
        }

        // ── /broadcast <message> (standard) ──────────────────────────────────
        String message = buildMessage(String.join(" ", args));
        Bukkit.broadcastMessage(message);
        return true;
    }

    /** Apply the configured format from messages.yml */
    private String buildMessage(String raw) {
        String format = plugin.getMessages().getRaw("admin.broadcast-format");
        return CoreEssentials.colorize(format.replace("{message}", raw));
    }

    /** Pad text with spaces so it appears centered in chat (~53 chars wide) */
    private String centerText(String text) {
        // Strip color codes to measure real length
        String stripped = text.replaceAll("§[0-9a-fk-or]", "");
        int chatWidth   = 53;
        int msgLength   = stripped.length();
        if (msgLength >= chatWidth) return text;
        int spaces = (chatWidth - msgLength) / 2;
        return " ".repeat(spaces) + text;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━ §eBroadcast §6§l━━━");
        sender.sendMessage("§e/bc §f<message>                     §7Broadcast standard");
        sender.sendMessage("§e/bc --title §f<titre> [sous-titre]  §7Titre à l'écran");
        sender.sendMessage("§e/bc --bar §f<message>               §7Action bar");
        sender.sendMessage("§e/bc --sound §f<SON> <message>       §7Broadcast + son");
        sender.sendMessage("§e/bc --world §f<monde> <message>     §7Broadcast dans un monde");
        sender.sendMessage("§e/bc --player §f<joueur> <message>   §7Message privé stylé");
        sender.sendMessage("§e/bc --prefix §f<préfixe> <message>  §7Préfixe personnalisé");
        sender.sendMessage("§e/bc --center §f<message>            §7Texte centré");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
    }
}

