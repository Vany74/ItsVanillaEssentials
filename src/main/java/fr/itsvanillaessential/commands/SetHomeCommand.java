package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public SetHomeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.sethome")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        String name = args.length > 0 ? args[0] : "home";
        int max = plugin.getHomeManager().getMaxHomes(p);
        int current = plugin.getHomeManager().getHomes(p.getUniqueId()).size();
        boolean exists = plugin.getHomeManager().getHome(p.getUniqueId(), name) != null;

        if (!exists && current >= max) {
            p.sendMessage(plugin.prefix() + "§cVous avez atteint la limite de §e" + max + " §chomes.");
            return true;
        }
        plugin.getHomeManager().setHome(p.getUniqueId(), name, p.getLocation());
        p.sendMessage(plugin.prefix() + "§aHome §e" + name + " §adéfini !");
        return true;
    }
}
