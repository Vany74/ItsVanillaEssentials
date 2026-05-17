package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public KickCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.kick")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /kick <joueur> [raison]"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Expulsé par un administrateur";
        target.kickPlayer(CoreEssentials.colorize("§cVous avez été expulsé.\n§7Raison: §e" + reason));
        plugin.getServer().broadcastMessage(plugin.prefix() + "§e" + target.getName() + " §ca été expulsé. §7(" + reason + ")");
        return true;
    }
}
