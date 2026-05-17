package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WarnCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WarnCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.warn")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /warn <joueur> <raison>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getModerationManager().addWarning(target.getUniqueId(), sender.getName(), reason);
        int count = plugin.getModerationManager().getWarningCount(target.getUniqueId());
        target.sendMessage(plugin.prefix() + "§c⚠ Avertissement #" + count + ": §e" + reason);
        sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §aaverti §7(" + count + " avert. total).");
        plugin.getServer().broadcastMessage(plugin.prefix() + "§e" + target.getName() + " §ca reçu un avertissement. §7(" + reason + ")");
        return true;
    }
}
