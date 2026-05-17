package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public DelHomeCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.delhome")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /delhome <nom>"); return true; }
        if (plugin.getHomeManager().deleteHome(p.getUniqueId(), args[0])) {
            p.sendMessage(plugin.prefix() + "§aHome §e" + args[0] + " §asupprimé.");
        } else {
            p.sendMessage(plugin.prefix() + "§cHome §e" + args[0] + " §cintrouvable.");
        }
        return true;
    }
}
