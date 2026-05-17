package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public FlyCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.fly")) {
            sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Player target;
        if (args.length > 0 && sender.hasPermission("core.fly.others")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }
        target.setAllowFlight(!target.getAllowFlight());
        boolean flying = target.getAllowFlight();
        target.sendMessage(plugin.prefix() + "§fVol: " + (flying ? "§aACTIVÉ" : "§cDÉSACTIVÉ"));
        if (sender != target) sender.sendMessage(plugin.prefix() + "§fVol de §e" + target.getName() + ": " + (flying ? "§aACTIVÉ" : "§cDÉSACTIVÉ"));
        return true;
    }
}
