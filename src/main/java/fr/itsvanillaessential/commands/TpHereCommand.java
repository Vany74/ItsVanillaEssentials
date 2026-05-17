package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpHereCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TpHereCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.tphere")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /tphere <joueur>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }
        target.teleport(p.getLocation());
        p.sendMessage(plugin.prefix() + "§e" + target.getName() + " §aa été téléporté vers vous.");
        target.sendMessage(plugin.prefix() + "§eVous avez été téléporté(e) vers §f" + p.getName() + "§e.");
        return true;
    }
}
