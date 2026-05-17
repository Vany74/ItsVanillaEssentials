package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public SpeedCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.speed")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /speed <1-10>"); return true; }
        int lvl;
        try { lvl = Integer.parseInt(args[0]); } catch (NumberFormatException e) {
            p.sendMessage(plugin.prefix() + "§cNombre invalide."); return true;
        }
        lvl = Math.max(1, Math.min(10, lvl));
        float speed = lvl / 10f;
        if (p.isFlying()) p.setFlySpeed(speed);
        else p.setWalkSpeed(speed);
        p.sendMessage(plugin.prefix() + "§aVitesse réglée à §e" + lvl + "§a.");
        return true;
    }
}
