package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InvSeeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public InvSeeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.invsee")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /invsee <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        p.openInventory(target.getInventory());
        p.sendMessage(plugin.prefix() + "§aInventaire de §e" + target.getName() + "§a.");
        return true;
    }
}
