package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class NickCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public NickCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.nick")) { p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /nick <pseudo|off>"); return true; }
        if (args[0].equalsIgnoreCase("off")) {
            plugin.getPlayerDataManager().removeNick(p.getUniqueId());
            p.setDisplayName(p.getName());
            p.sendMessage(plugin.prefix() + "§aPseudo réinitialisé.");
            return true;
        }
        int maxLen = plugin.getConfig().getInt("nick.max-length", 16);
        String raw = args[0];
        if (raw.length() > maxLen) { p.sendMessage(plugin.prefix() + "§cPseudo trop long (max §e" + maxLen + " §ccaractères)."); return true; }
        String nick = p.hasPermission("core.nick.color") && plugin.getConfig().getBoolean("nick.allow-colors", true)
                ? CoreEssentials.colorize(raw) : raw;
        plugin.getPlayerDataManager().setNick(p.getUniqueId(), raw);
        p.setDisplayName(nick);
        p.sendMessage(plugin.prefix() + "§aPseudo changé en: " + nick);
        return true;
    }
}
