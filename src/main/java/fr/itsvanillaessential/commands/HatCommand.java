package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HatCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public HatCommand(CoreEssentials plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.hat")) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) { p.sendMessage(plugin.prefix() + "§cTenez un item en main."); return true; }
        ItemStack oldHelmet = p.getInventory().getHelmet();
        p.getInventory().setHelmet(hand.clone());
        p.getInventory().setItemInMainHand(oldHelmet);
        p.sendMessage(plugin.prefix() + "§aCoiffure mise !");
        return true;
    }
}
