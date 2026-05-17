package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public GamemodeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.gamemode")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }

        GameMode gm = switch (label.toLowerCase()) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> {
                if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /gamemode <survival|creative|adventure|spectator> [joueur]"); yield null; }
                yield switch (args[0].toLowerCase()) {
                    case "survival", "s", "0" -> GameMode.SURVIVAL;
                    case "creative", "c", "1" -> GameMode.CREATIVE;
                    case "adventure", "a", "2" -> GameMode.ADVENTURE;
                    case "spectator", "sp", "3" -> GameMode.SPECTATOR;
                    default -> null;
                };
            }
        };

        if (gm == null) { sender.sendMessage(plugin.prefix() + "§cGamemode invalide."); return true; }

        Player target;
        int targetArgIndex = label.equalsIgnoreCase("gamemode") ? 1 : 0;
        if (args.length > targetArgIndex && sender.hasPermission("core.gamemode.others")) {
            target = plugin.getServer().getPlayer(args[targetArgIndex]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }

        target.setGameMode(gm);
        target.sendMessage(plugin.prefix() + "§aGamemode: §e" + gm.name().toLowerCase());
        if (sender != target) sender.sendMessage(plugin.prefix() + "§aGamemode de §e" + target.getName() + " §arégié à §e" + gm.name().toLowerCase() + "§a.");
        return true;
    }
}
