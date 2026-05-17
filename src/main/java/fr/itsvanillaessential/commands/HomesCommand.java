package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomesCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public HomesCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.home")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        var homes = plugin.getHomeManager().getHomes(p.getUniqueId());
        if (homes.isEmpty()) {
            p.sendMessage(plugin.prefix() + "§cVous n'avez aucun home. Utilisez §f/sethome <nom>§c.");
            return true;
        }
        p.sendMessage(plugin.prefix() + "§6Vos homes §7(" + homes.size() + "/" + plugin.getHomeManager().getMaxHomes(p) + ")§6:");
        homes.keySet().forEach(name -> p.sendMessage("  §7- §e" + name));
        return true;
    }
}
