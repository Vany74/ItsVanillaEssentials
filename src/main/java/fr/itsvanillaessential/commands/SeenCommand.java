package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SeenCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public SeenCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.seen")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /seen <joueur>"); return true; }
        Player online = plugin.getServer().getPlayer(args[0]);
        if (online != null) {
            sender.sendMessage(plugin.prefix() + "§e" + online.getName() + " §aest actuellement connecté.");
            return true;
        }
        long last = plugin.getPlayerDataManager().getLastSeen(args[0]);
        if (last == -1) { sender.sendMessage(plugin.prefix() + "§cJoueur inconnu."); return true; }
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(last));
        sender.sendMessage(plugin.prefix() + "§e" + args[0] + " §fvu pour la dernière fois le §7" + date + "§f.");
        return true;
    }
}
