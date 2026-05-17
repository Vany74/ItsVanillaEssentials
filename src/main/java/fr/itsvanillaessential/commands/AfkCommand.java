package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public AfkCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.afk")) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        boolean afk = !plugin.getPlayerDataManager().isAfk(p.getUniqueId());
        plugin.getPlayerDataManager().setAfk(p.getUniqueId(), afk);
        String suffix = CoreEssentials.colorize(plugin.getConfig().getString("chat.afk-suffix", " §7[AFK]"));
        if (afk) {
            p.setDisplayName(p.getDisplayName() + suffix);
            plugin.getServer().broadcastMessage(plugin.prefix() + "§e" + p.getName() + " §fest maintenant AFK.");
        } else {
            p.setDisplayName(p.getDisplayName().replace(suffix, ""));
            plugin.getServer().broadcastMessage(plugin.prefix() + "§e" + p.getName() + " §fn'est plus AFK.");
        }
        return true;
    }
}
