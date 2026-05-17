package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class MaceAnnounceListener implements Listener {

    private final CoreEssentials plugin;

    public MaceAnnounceListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    // ─── CRAFT ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMaceCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRecipe().getResult().getType() != Material.MACE) return;
        if (!plugin.getConfig().getBoolean("mace.announce-craft", true)) return;

        String teamInfo = getTeamInfo(player);

        String format = plugin.getConfig().getString(
                "mace.announce-craft-format",
                "&6&l⚔ MACE CRAFTÉE ! &e{player}{team} &6a forgé une &lMACE &6!");
        String msg = CoreEssentials.colorize(
                format.replace("{player}", player.getName())
                      .replace("{team}", teamInfo));

        broadcastStyled(msg);
    }

    // ─── DESTRUCTION (durabilité à 0) ─────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMaceBreak(PlayerItemBreakEvent event) {
        if (event.getBrokenItem().getType() != Material.MACE) return;
        if (!plugin.getConfig().getBoolean("mace.announce-destroy", true)) return;

        Player player = event.getPlayer();
        String teamInfo = getTeamInfo(player);

        String format = plugin.getConfig().getString(
                "mace.announce-destroy-format",
                "&c&l💥 MACE DÉTRUITE ! &e{player}{team} &ca perdu sa &lMACE &c!");
        String msg = CoreEssentials.colorize(
                format.replace("{player}", player.getName())
                      .replace("{team}", teamInfo));

        broadcastStyled(msg);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String getTeamInfo(Player player) {
        var team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (team == null) return "";
        String color = plugin.getTeamManager().formatColor(team.color);
        return " " + color + "[" + team.displayName + "]";
    }

    private void broadcastStyled(String msg) {
        // Top separator
        String sep = CoreEssentials.colorize("&8&m" + "─".repeat(40));
        Bukkit.broadcastMessage(sep);
        Bukkit.broadcastMessage(msg);
        Bukkit.broadcastMessage(sep);

        // Play sound to all
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } catch (Exception ignored) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
            }
        }
    }
}
