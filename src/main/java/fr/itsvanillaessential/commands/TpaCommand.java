package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TpaCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.tpa")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /tpa <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || target == p) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }
        plugin.getTeleportManager().sendTpaRequest(p, target);
        p.sendMessage(plugin.prefix() + "§aDemande envoyée à §e" + target.getName() + "§a.");
        target.sendMessage(plugin.prefix() + "§e" + p.getName() + " §fveut se téléporter à vous. "
                + "§a/tpaccept §7ou §c/tpdeny");
        return true;
    }
}
