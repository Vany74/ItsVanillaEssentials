package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public GodCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.god")) {
            sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        Player target;
        if (args.length > 0 && sender.hasPermission("core.god.others")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else { sender.sendMessage("Spécifiez un joueur."); return true; }
        boolean god = !plugin.getPlayerDataManager().isGod(target.getUniqueId());
        plugin.getPlayerDataManager().setGod(target.getUniqueId(), god);
        target.sendMessage(plugin.prefix() + "§fMode God: " + (god ? "§aACTIVÉ" : "§cDÉSACTIVÉ"));
        if (sender != target) sender.sendMessage(plugin.prefix() + "§fGod de §e" + target.getName() + ": " + (god ? "§aACTIVÉ" : "§cDÉSACTIVÉ"));
        return true;
    }
}
