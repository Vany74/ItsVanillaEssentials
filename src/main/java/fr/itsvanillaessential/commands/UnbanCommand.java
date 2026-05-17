package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.BanList;
import org.bukkit.command.*;

public class UnbanCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public UnbanCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.unban")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /unban <joueur>"); return true; }
        String name = args[0];
        if (plugin.getServer().getBanList(BanList.Type.NAME).isBanned(name)) {
            plugin.getServer().getBanList(BanList.Type.NAME).pardon(name);
            sender.sendMessage(plugin.prefix() + "§e" + name + " §adébanni avec succès.");
        } else {
            sender.sendMessage(plugin.prefix() + "§e" + name + " §cn'est pas banni.");
        }
        return true;
    }
}
