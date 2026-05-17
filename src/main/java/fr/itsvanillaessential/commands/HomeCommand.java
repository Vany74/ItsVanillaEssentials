package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public HomeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.home")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        String name = args.length > 0 ? args[0] : "home";
        Location loc = plugin.getHomeManager().getHome(p.getUniqueId(), name);
        if (loc == null) {
            p.sendMessage(plugin.prefix() + "§cHome §e" + name + " §cintrouvable. Utilisez §f/homes §cpour voir la liste.");
            return true;
        }
        plugin.getTeleportManager().teleport(p, loc);
        p.sendMessage(plugin.prefix() + "§aTéléporté au home §e" + name + "§a.");
        return true;
    }
}
