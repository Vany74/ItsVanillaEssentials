package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public BackCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.back")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Location back = plugin.getPlayerDataManager().getBackLocation(p.getUniqueId());
        if (back == null) { p.sendMessage(plugin.prefix() + "§cAucune position précédente enregistrée."); return true; }
        plugin.getTeleportManager().teleport(p, back);
        p.sendMessage(plugin.prefix() + "§aTéléporté à votre dernière position.");
        return true;
    }
}
