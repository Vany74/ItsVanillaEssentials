package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TimeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TimeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.time")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /time <day|night|noon|midnight|<ticks>>"); return true; }
        long time = switch (args[0].toLowerCase()) {
            case "day"      -> 1000L;
            case "noon"     -> 6000L;
            case "night"    -> 13000L;
            case "midnight" -> 18000L;
            default -> { try { yield Long.parseLong(args[0]); } catch (NumberFormatException e) { yield -1L; } }
        };
        if (time < 0) { sender.sendMessage(plugin.prefix() + "§cValeur invalide."); return true; }
        if (sender instanceof Player p) {
            p.getWorld().setTime(time);
        } else {
            plugin.getServer().getWorlds().forEach(w -> w.setTime(time));
        }
        sender.sendMessage(plugin.prefix() + "§aHeure réglée à §e" + time + "§a.");
        return true;
    }
}
