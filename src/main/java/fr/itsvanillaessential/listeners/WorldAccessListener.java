package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldAccessListener implements Listener {

    private final CoreEssentials plugin;

    public WorldAccessListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        String dest = event.getTo() != null ? event.getTo().getWorld().getName() : "";

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            boolean netherEnabled = plugin.getConfig().getBoolean("worlds.nether-enabled", true);
            if (!netherEnabled && !event.getPlayer().hasPermission("core.bypass.worldaccess")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.prefix()
                        + CoreEssentials.colorize("&cL'accès au &4Nether &cest désactivé sur ce serveur."));
            }
        } else if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            boolean endEnabled = plugin.getConfig().getBoolean("worlds.end-enabled", true);
            if (!endEnabled && !event.getPlayer().hasPermission("core.bypass.worldaccess")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.prefix()
                        + CoreEssentials.colorize("&cL'accès au &5End &cest désactivé sur ce serveur."));
            }
        }
    }
}
