package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.ModerationManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MuteCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public MuteCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.mute")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /mute <joueur> [durée]"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        long durationMs = args.length > 1 ? ModerationManager.parseDuration(args[1]) : 0L;
        plugin.getModerationManager().mute(target.getUniqueId(), durationMs);
        String dur = durationMs > 0 ? ModerationManager.formatDuration(durationMs) : "permanent";
        target.sendMessage(plugin.prefix() + "§cVous avez été rendu(e) muet(te) §7(" + dur + ").");
        sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §fmuté §7(" + dur + ").");
        return true;
    }
}
