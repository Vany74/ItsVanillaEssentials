package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DelWarpCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public DelWarpCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.delwarp")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /delwarp <nom>"); return true; }
        if (plugin.getWarpManager().deleteWarp(args[0])) {
            p.sendMessage(plugin.prefix() + "§aWarp §e" + args[0] + " §asupprimé.");
        } else {
            p.sendMessage(plugin.prefix() + "§cWarp §e" + args[0] + " §cintrouvable.");
        }
        return true;
    }
}
