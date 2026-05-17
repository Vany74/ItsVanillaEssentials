package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public UnmuteCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.unmute")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /unmute <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        if (!plugin.getModerationManager().isMuted(target.getUniqueId())) {
            sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §cn'est pas muté.");
            return true;
        }
        plugin.getModerationManager().unmute(target.getUniqueId());
        target.sendMessage(plugin.prefix() + "§aVous n'êtes plus muté(e).");
        sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §adémuté.");
        return true;
    }
}
