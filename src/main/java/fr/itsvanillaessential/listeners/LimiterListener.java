package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.CraftLimiterManager;
import fr.itsvanillaessential.managers.ItemLimiterManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class LimiterListener implements Listener {

    private final CoreEssentials plugin;

    public LimiterListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CRAFT LIMITER
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getCraftLimiterManager().isEnabled()) return;

        ItemStack result = event.getRecipe().getResult();
        Material mat     = result.getType();

        if (!plugin.getCraftLimiterManager().hasLimit(mat)) return;

        CraftLimiterManager.CheckResult check =
                plugin.getCraftLimiterManager().canCraft(player, mat);

        switch (check) {
            case LIMIT_REACHED -> {
                event.setCancelled(true);
                player.sendMessage(plugin.prefix()
                        + CoreEssentials.colorize(plugin.getCraftLimiterManager().getBlockedMessage(mat)));
            }
            case NO_PERMISSION -> {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessages().noPermission());
            }
            case COOLDOWN -> {
                event.setCancelled(true);
                long remaining = plugin.getCraftLimiterManager()
                        .getRemainingCooldown(player.getUniqueId(), mat);
                player.sendMessage(plugin.prefix()
                        + "§cCooldown: &e" + remaining + "s &crestant.");
            }
            case ALLOWED -> {
                // Track after craft completes
                plugin.getCraftLimiterManager().incrementCount(player.getUniqueId(), mat);
                plugin.getCraftLimiterManager().setCooldown(player.getUniqueId(), mat);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEM LIMITER — Pickup
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Material mat = event.getItem().getItemStack().getType();
        if (!plugin.getItemLimiterManager().isEnabled()) return;
        if (!plugin.getItemLimiterManager().hasLimit(mat)) return;

        String bypass = plugin.getItemLimiterManager().getBypassPermission(mat);
        if (!bypass.isEmpty() && player.hasPermission(bypass)) return;

        // Pickup blocked mode
        if (plugin.getItemLimiterManager().isPickupBlocked(mat)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix()
                    + CoreEssentials.colorize(plugin.getItemLimiterManager().getBlockedMessage(mat)));
            return;
        }

        // Inventory limit
        int maxInv = plugin.getItemLimiterManager().getInventoryMax(mat);
        if (maxInv >= 0) {
            int current = plugin.getItemLimiterManager().countInInventory(player, mat);
            int pickupAmt = event.getItem().getItemStack().getAmount();
            if (current + pickupAmt > maxInv) {
                String action = plugin.getItemLimiterManager().getAction(mat);
                if (action.equalsIgnoreCase("deny")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.prefix()
                            + CoreEssentials.colorize(plugin.getItemLimiterManager().getBlockedMessage(mat)));
                } else if (action.equalsIgnoreCase("warn")) {
                    player.sendMessage(plugin.prefix()
                            + CoreEssentials.colorize(plugin.getItemLimiterManager().getBlockedMessage(mat)));
                }
            }
        }

        // Mace pickup check
        if (mat == Material.MACE && plugin.getMaceLimiterManager().isEnabled()) {
            if (plugin.getMaceLimiterManager().wouldExceedTeamLimit(player)) {
                event.setCancelled(true);
                player.sendMessage(plugin.prefix()
                        + plugin.getMaceLimiterManager().getTeamLimitMessage(player));
                return;
            }
            if (plugin.getMaceLimiterManager().wouldExceedPerPlayerLimit(player)) {
                event.setCancelled(true);
                player.sendMessage(plugin.prefix()
                        + CoreEssentials.colorize(plugin.getMaceLimiterManager().getPerPlayerMsg()));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEM LIMITER — Drop
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Material mat  = event.getItemDrop().getItemStack().getType();
        if (!plugin.getItemLimiterManager().isEnabled()) return;
        if (!plugin.getItemLimiterManager().hasLimit(mat)) return;
        String bypass = plugin.getItemLimiterManager().getBypassPermission(mat);
        if (!bypass.isEmpty() && player.hasPermission(bypass)) return;
        if (plugin.getItemLimiterManager().isDropBlocked(mat)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix()
                    + CoreEssentials.colorize(plugin.getItemLimiterManager().getBlockedMessage(mat)));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEM LIMITER — Held in hand
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) return;
        Material mat = item.getType();
        if (!plugin.getItemLimiterManager().isEnabled()) return;
        if (!plugin.getItemLimiterManager().hasLimit(mat)) return;
        String bypass = plugin.getItemLimiterManager().getBypassPermission(mat);
        if (!bypass.isEmpty() && player.hasPermission(bypass)) return;
        if (plugin.getItemLimiterManager().isHeldBlocked(mat)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix()
                    + CoreEssentials.colorize(plugin.getItemLimiterManager().getBlockedMessage(mat)));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MACE — Smash Attack (EntityDamageByEntity with fall velocity)
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.MACE) return;
        if (!plugin.getMaceLimiterManager().isEnabled()) return;
        if (!plugin.getMaceLimiterManager().isSmashEnabled()) return;

        // Disable smash attack entirely
        if (plugin.getMaceLimiterManager().isSmashDisabled()) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.prefix() + "§cLe smash attack est désactivé.");
            return;
        }

        // Disabled worlds
        String worldName = attacker.getWorld().getName();
        if (plugin.getMaceLimiterManager().getSmashDisabledWorlds().contains(worldName)) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.prefix() + "§cLe smash attack est désactivé dans ce monde.");
            return;
        }

        // Cooldown check
        if (attacker.getFallDistance() > 0 && plugin.getMaceLimiterManager().getSmashCooldownSeconds() > 0) {
            if (plugin.getMaceLimiterManager().isSmashOnCooldown(attacker.getUniqueId())) {
                event.setCancelled(true);
                long remaining = plugin.getMaceLimiterManager().getRemainingSmashCooldown(attacker.getUniqueId());
                attacker.sendMessage(plugin.prefix()
                        + CoreEssentials.colorize(plugin.getMaceLimiterManager().getSmashBlockedMsg())
                        + " §7(" + remaining + "s)");
                return;
            }
            plugin.getMaceLimiterManager().setSmashCooldown(attacker.getUniqueId());
        }

        // Max damage cap
        double maxDmg = plugin.getMaceLimiterManager().getMaxSmashDamage();
        if (maxDmg > 0 && event.getDamage() > maxDmg) {
            event.setDamage(maxDmg);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MACE — Craft via crafting table
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMaceCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRecipe().getResult().getType() != Material.MACE) return;
        if (!plugin.getMaceLimiterManager().isEnabled()) return;

        if (plugin.getMaceLimiterManager().wouldExceedTeamLimit(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix()
                    + plugin.getMaceLimiterManager().getTeamLimitMessage(player));
            return;
        }
        if (plugin.getMaceLimiterManager().wouldExceedPerPlayerLimit(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix()
                    + CoreEssentials.colorize(
                        plugin.getMaceLimiterManager().getRaw("messages.craft-denied")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MACE — Banned worlds (can't hold mace)
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMaceHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || item.getType() != Material.MACE) return;
        if (!plugin.getMaceLimiterManager().isEnabled()) return;
        if (plugin.getMaceLimiterManager().getBannedWorlds().contains(player.getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage(plugin.prefix() + "§cLa mace est interdite dans ce monde.");
        }
    }
}
