package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public EnderChestCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.enderchest")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length > 0 && p.hasPermission("core.enderchest.others")) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
            p.openInventory(target.getEnderChest());
            return true;
        }
        p.openInventory(p.getEnderChest());
        return true;
    }
}
