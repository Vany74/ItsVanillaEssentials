package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ClearWarningsCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public ClearWarningsCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.clearwarnings")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /clearwarnings <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        plugin.getModerationManager().clearWarnings(target.getUniqueId());
        sender.sendMessage(plugin.prefix() + "§aAvertissements de §e" + target.getName() + " §aeffacés.");
        target.sendMessage(plugin.prefix() + "§aVos avertissements ont été effacés.");
        return true;
    }
}
