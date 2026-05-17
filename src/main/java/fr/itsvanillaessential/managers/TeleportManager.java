package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final CoreEssentials plugin;

    // Pending TPA: requester UUID -> target UUID
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();
    // Pending TPA expire tasks
    private final Map<UUID, BukkitTask> tpaTasks = new HashMap<>();

    public TeleportManager(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleport with optional countdown (checks if player moves).
     */
    public void teleport(Player player, Location destination) {
        int delay = plugin.getConfig().getInt("teleport.delay", 3);
        if (delay <= 0 || player.hasPermission("core.tp.instant")) {
            player.teleport(destination);
            return;
        }

        Location startLoc = player.getLocation().clone();
        player.sendMessage(plugin.prefix() + CoreEssentials.colorize(
                plugin.getConfig().getString("messages.tp-delay", "&eTéléportation dans &6{delay}s&e...")
                        .replace("{delay}", String.valueOf(delay))));

        new BukkitRunnable() {
            int secondsLeft = delay;
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                // Check if moved
                if (hasMoved(startLoc, player.getLocation())) {
                    cancel();
                    player.sendMessage(plugin.prefix() + CoreEssentials.colorize(
                            plugin.getConfig().getString("messages.tp-cancelled", "&cTéléportation annulée.")));
                    return;
                }
                secondsLeft--;
                if (secondsLeft <= 0) {
                    cancel();
                    player.teleport(destination);
                    player.sendMessage(plugin.prefix() + CoreEssentials.colorize(
                            plugin.getConfig().getString("messages.teleported", "&aTéléporté !")));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean hasMoved(Location a, Location b) {
        return Math.abs(a.getX() - b.getX()) > 0.5
            || Math.abs(a.getY() - b.getY()) > 0.5
            || Math.abs(a.getZ() - b.getZ()) > 0.5;
    }

    // TPA
    public void sendTpaRequest(Player requester, Player target) {
        UUID rId = requester.getUniqueId();
        // Cancel existing
        if (tpaTasks.containsKey(rId)) tpaTasks.get(rId).cancel();

        tpaRequests.put(rId, target.getUniqueId());

        int timeout = plugin.getConfig().getInt("teleport.tpa-timeout", 60);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.containsKey(rId)) {
                    tpaRequests.remove(rId);
                    if (requester.isOnline())
                        requester.sendMessage(plugin.prefix() + "§cVotre demande de tp a expiré.");
                }
            }
        }.runTaskLater(plugin, timeout * 20L);
        tpaTasks.put(rId, task);
    }

    public UUID getPendingTpaTarget(UUID requester)   { return tpaRequests.get(requester); }

    /** Returns the requester UUID who sent a TPA to the given target, or null. */
    public UUID getPendingTpaRequester(UUID target) {
        for (Map.Entry<UUID, UUID> e : tpaRequests.entrySet()) {
            if (e.getValue().equals(target)) return e.getKey();
        }
        return null;
    }

    public void removeTpaRequest(UUID requester) {
        tpaRequests.remove(requester);
        BukkitTask t = tpaTasks.remove(requester);
        if (t != null) t.cancel();
    }
}
