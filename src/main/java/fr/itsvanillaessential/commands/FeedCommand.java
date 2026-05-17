package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FeedCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public FeedCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.feed")) {
            sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Player target;
        if (args.length > 0 && sender.hasPermission("core.feed.others")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.sendMessage(plugin.prefix() + "§aVous avez été nourri(e) !");
        if (sender != target) sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §anourri.");
        return true;
    }
}
