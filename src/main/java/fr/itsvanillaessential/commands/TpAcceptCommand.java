package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TpAcceptCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        UUID requesterUUID = plugin.getTeleportManager().getPendingTpaRequester(p.getUniqueId());
        if (requesterUUID == null) { p.sendMessage(plugin.prefix() + "§cAucune demande de tp en attente."); return true; }
        Player requester = Bukkit.getPlayer(requesterUUID);
        plugin.getTeleportManager().removeTpaRequest(requesterUUID);
        if (requester == null || !requester.isOnline()) {
            p.sendMessage(plugin.prefix() + "§cLe joueur n'est plus connecté.");
            return true;
        }
        requester.teleport(p.getLocation());
        requester.sendMessage(plugin.prefix() + "§aTéléportation acceptée par §e" + p.getName() + "§a !");
        p.sendMessage(plugin.prefix() + "§aVous avez accepté la demande de §e" + requester.getName() + "§a.");
        return true;
    }
}
