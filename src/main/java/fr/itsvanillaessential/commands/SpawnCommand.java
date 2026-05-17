package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public SpawnCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(CoreEssentials.colorize(plugin.getConfig().getString("messages.player-only")));
            return true;
        }
        if (!p.hasPermission("core.spawn")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Location spawn = getSpawnLocation();
        if (spawn == null) {
            p.sendMessage(plugin.prefix() + "§cSpawn non défini ! Utilisez /setspawn.");
            return true;
        }
        plugin.getTeleportManager().teleport(p, spawn);
        return true;
    }

    private Location getSpawnLocation() {
        String worldName = plugin.getConfig().getString("spawn.world", "world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        double x = plugin.getConfig().getDouble("spawn.x", 0.5);
        double y = plugin.getConfig().getDouble("spawn.y", 64.0);
        double z = plugin.getConfig().getDouble("spawn.z", 0.5);
        float yaw   = (float) plugin.getConfig().getDouble("spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
