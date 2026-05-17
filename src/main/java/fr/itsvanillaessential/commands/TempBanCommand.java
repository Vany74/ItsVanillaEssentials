package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.ModerationManager;
import org.bukkit.BanList;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Date;

public class TempBanCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TempBanCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.tempban")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /tempban <joueur> <durée> [raison]\n§7Exemples de durée: 1d, 2h, 30m"); return true; }
        String name = args[0];
        long durationMs = ModerationManager.parseDuration(args[1]);
        if (durationMs <= 0) { sender.sendMessage(plugin.prefix() + "§cDurée invalide. Exemples: §e1d§c, §e2h§c, §e30m"); return true; }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Ban temporaire";
        Date expiry = new Date(System.currentTimeMillis() + durationMs);
        plugin.getServer().getBanList(BanList.Type.NAME).addBan(name, reason, expiry, sender.getName());
        Player target = plugin.getServer().getPlayer(name);
        String durationStr = ModerationManager.formatDuration(durationMs);
        if (target != null) target.kickPlayer(CoreEssentials.colorize("§cBan temporaire: §e" + durationStr + "\n§7Raison: §e" + reason));
        plugin.getServer().broadcastMessage(plugin.prefix() + "§c" + name + " §fbanni pour §e" + durationStr + "§7. (" + reason + ")");
        return true;
    }
}
