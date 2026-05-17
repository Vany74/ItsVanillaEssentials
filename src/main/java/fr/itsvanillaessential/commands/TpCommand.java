package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TpCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.tp")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /tp <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }
        plugin.getTeleportManager().teleport(p, target.getLocation());
        p.sendMessage(plugin.prefix() + "§aTéléporté vers §e" + target.getName() + "§a.");
        return true;
    }
}
