package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WarpCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.warp")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) {
            var warps = plugin.getWarpManager().getWarpNames();
            if (warps.isEmpty()) { p.sendMessage(plugin.prefix() + "§cAucun warp disponible."); return true; }
            p.sendMessage(plugin.prefix() + "§6Warps disponibles: §e" + String.join("§7, §e", warps));
            return true;
        }
        Location loc = plugin.getWarpManager().getWarp(args[0]);
        if (loc == null) { p.sendMessage(plugin.prefix() + "§cWarp §e" + args[0] + " §cintrouvable."); return true; }
        plugin.getTeleportManager().teleport(p, loc);
        p.sendMessage(plugin.prefix() + "§aTéléporté au warp §e" + args[0] + "§a.");
        return true;
    }
}
