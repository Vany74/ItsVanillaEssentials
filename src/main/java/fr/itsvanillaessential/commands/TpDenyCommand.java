package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public TpDenyCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        UUID requesterUUID = plugin.getTeleportManager().getPendingTpaRequester(p.getUniqueId());
        if (requesterUUID == null) { p.sendMessage(plugin.prefix() + "§cAucune demande en attente."); return true; }
        Player requester = Bukkit.getPlayer(requesterUUID);
        plugin.getTeleportManager().removeTpaRequest(requesterUUID);
        if (requester != null) requester.sendMessage(plugin.prefix() + "§cVotre demande a été refusée par §e" + p.getName() + "§c.");
        p.sendMessage(plugin.prefix() + "§cDemande refusée.");
        return true;
    }
}
