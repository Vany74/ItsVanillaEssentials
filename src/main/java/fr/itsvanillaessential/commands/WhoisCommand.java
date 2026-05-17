package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class WhoisCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WhoisCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.whois")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /whois <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        String nick = plugin.getPlayerDataManager().getNick(target.getUniqueId());
        sender.sendMessage("§6§l━━━━━ §eWhois: " + target.getName() + " §6§l━━━━━");
        sender.sendMessage("§7UUID: §f" + target.getUniqueId());
        sender.sendMessage("§7IP: §f" + (target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "N/A"));
        sender.sendMessage("§7Nick: §f" + (nick != null ? nick : "§7Aucun"));
        sender.sendMessage("§7Gamemode: §f" + target.getGameMode().name().toLowerCase());
        sender.sendMessage("§7Monde: §f" + target.getWorld().getName());
        sender.sendMessage("§7Position: §f" + (int)target.getLocation().getX() + ", " + (int)target.getLocation().getY() + ", " + (int)target.getLocation().getZ());
        sender.sendMessage("§7Ping: §f" + target.getPing() + "ms");
        sender.sendMessage("§7God: §f" + (plugin.getPlayerDataManager().isGod(target.getUniqueId()) ? "§aOui" : "§cNon"));
        sender.sendMessage("§7AFK: §f" + (plugin.getPlayerDataManager().isAfk(target.getUniqueId()) ? "§aOui" : "§cNon"));
        sender.sendMessage("§7Avertissements: §f" + plugin.getModerationManager().getWarningCount(target.getUniqueId()));
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return true;
    }
}
