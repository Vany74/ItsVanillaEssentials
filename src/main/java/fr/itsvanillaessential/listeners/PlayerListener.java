package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final CoreEssentials plugin;

    public PlayerListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        // Restore nickname display name
        String nick = plugin.getPlayerDataManager().getNick(p.getUniqueId());
        if (nick != null) p.setDisplayName(CoreEssentials.colorize(nick));

        // Show AFK suffix
        plugin.getPlayerDataManager().setAfk(p.getUniqueId(), false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        plugin.getPlayerDataManager().saveLastSeen(p);
        // Cancel pending TPA
        plugin.getTeleportManager().removeTpaRequest(p.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (p.getLastDeathLocation() != null) {
            plugin.getPlayerDataManager().setBackLocation(p.getUniqueId(), p.getLastDeathLocation());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Back is set on death above; on respawn we don't override it
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Store pre-teleport location as /back
        Player p = event.getPlayer();
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) return;
        plugin.getPlayerDataManager().setBackLocation(p.getUniqueId(), event.getFrom());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (plugin.getPlayerDataManager().isGod(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        // Remove AFK if they move
        if (plugin.getPlayerDataManager().isAfk(p.getUniqueId())) {
            if (event.getFrom().distance(event.getTo()) > 0.1) {
                plugin.getPlayerDataManager().setAfk(p.getUniqueId(), false);
                String suffix = CoreEssentials.colorize(plugin.getConfig().getString("chat.afk-suffix", " §7[AFK]"));
                String display = p.getDisplayName().replace(suffix, "");
                p.setDisplayName(display);
                p.getWorld().getPlayers().forEach(pl ->
                    pl.sendMessage(plugin.prefix() + "§e" + p.getName() + " §fn'est plus AFK.")
                );
            }
        }
    }
}
