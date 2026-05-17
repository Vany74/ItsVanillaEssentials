package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class WarningsCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WarningsCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.warn")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /warnings <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        List<String> warnings = plugin.getModerationManager().getWarnings(target.getUniqueId());
        if (warnings.isEmpty()) {
            sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §fn'a aucun avertissement.");
            return true;
        }
        sender.sendMessage(plugin.prefix() + "§6Avertissements de §e" + target.getName() + " §7(" + warnings.size() + ")§6:");
        warnings.forEach(sender::sendMessage);
        return true;
    }
}
