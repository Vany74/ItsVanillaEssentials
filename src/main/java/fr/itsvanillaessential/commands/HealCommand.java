package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public HealCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.heal")) {
            sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Player target;
        if (args.length > 0 && sender.hasPermission("core.heal.others")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }

        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.sendMessage(plugin.prefix() + "§aVous avez été soigné(e) !");
        if (sender != target) sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §asoigné.");
        return true;
    }
}
