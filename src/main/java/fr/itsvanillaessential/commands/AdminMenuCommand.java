package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.gui.AdminMenuGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AdminMenuCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public AdminMenuCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getMessages().playerOnly());
            return true;
        }
        if (!p.hasPermission("core.adminmenu")) {
            p.sendMessage(plugin.getMessages().noPermission());
            return true;
        }
        new AdminMenuGUI(plugin, p).open();
        return true;
    }
}
