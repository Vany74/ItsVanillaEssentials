package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class WorkbenchCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WorkbenchCommand(CoreEssentials plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.workbench")) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        p.openWorkbench(null, true);
        return true;
    }
}
