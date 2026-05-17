package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ClearCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public ClearCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.clear")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        Player target;
        if (args.length > 0 && sender.hasPermission("core.clear.others")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }
        target.getInventory().clear();
        target.sendMessage(plugin.prefix() + "§aInventaire vidé.");
        if (sender != target) sender.sendMessage(plugin.prefix() + "§aInventaire de §e" + target.getName() + " §avidé.");
        return true;
    }
}
