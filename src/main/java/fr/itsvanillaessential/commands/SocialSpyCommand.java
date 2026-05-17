package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public SocialSpyCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.socialspy")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        plugin.getPlayerDataManager().toggleSocialSpy(p.getUniqueId());
        boolean on = plugin.getPlayerDataManager().hasSocialSpy(p.getUniqueId());
        p.sendMessage(plugin.prefix() + "§fSocialSpy: " + (on ? "§aACTIVÉ" : "§cDÉSACTIVÉ"));
        return true;
    }
}
