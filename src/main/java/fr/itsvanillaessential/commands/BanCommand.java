package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.ModerationManager;
import org.bukkit.BanList;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Date;

public class BanCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public BanCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.ban")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /ban <joueur> [raison]"); return true; }
        String name = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banni par un administrateur";
        plugin.getServer().getBanList(BanList.Type.NAME).addBan(name, reason, (Date) null, sender.getName());
        Player target = plugin.getServer().getPlayer(name);
        if (target != null) target.kickPlayer(CoreEssentials.colorize("§cVous avez été banni.\n§7Raison: §e" + reason));
        plugin.getServer().broadcastMessage(plugin.prefix() + "§c" + name + " §fa été banni. §7(" + reason + ")");
        return true;
    }
}
