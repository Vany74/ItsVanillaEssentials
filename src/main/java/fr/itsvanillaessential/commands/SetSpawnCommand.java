package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public SetSpawnCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }
        if (!p.hasPermission("core.setspawn")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Location loc = p.getLocation();
        plugin.getConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.x", loc.getX());
        plugin.getConfig().set("spawn.y", loc.getY());
        plugin.getConfig().set("spawn.z", loc.getZ());
        plugin.getConfig().set("spawn.yaw", loc.getYaw());
        plugin.getConfig().set("spawn.pitch", loc.getPitch());
        plugin.saveConfig();
        p.sendMessage(plugin.prefix() + "§aSpawn défini à votre position !");
        return true;
    }
}
