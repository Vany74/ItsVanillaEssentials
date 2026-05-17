package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public SetWarpCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.setwarp")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /setwarp <nom>"); return true; }
        plugin.getWarpManager().setWarp(args[0], p.getLocation());
        p.sendMessage(plugin.prefix() + "§aWarp §e" + args[0] + " §acréé !");
        return true;
    }
}
