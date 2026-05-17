package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class RepairCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public RepairCommand(CoreEssentials plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.repair")) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) { p.sendMessage(plugin.prefix() + "§cTenez un item en main."); return true; }
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable d) {
            d.setDamage(0);
            item.setItemMeta(meta);
            p.sendMessage(plugin.prefix() + "§aItem réparé !");
        } else {
            p.sendMessage(plugin.prefix() + "§cCet item ne peut pas être réparé.");
        }
        return true;
    }
}
