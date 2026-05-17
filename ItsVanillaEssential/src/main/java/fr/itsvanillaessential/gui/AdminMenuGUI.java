package fr.itsvanillaessential.gui;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Menu Admin — 6 rangées (54 slots)
 *
 *  PAGE : MAIN
 *  ┌─────────────────────────────────────────────┐
 *  │ BORDURE (verre gris)                        │
 *  │  [PvP]  [END]  [NETHER]  [MÉTÉO] [HEURE]   │
 *  │  [CRAFT LIMITER] [ITEM LIMITER] [MACE]      │
 *  │  [TEAMS]  [COMMAND BLOCKER]  [ANNONCES]     │
 *  │  [RELOAD ALL]                               │
 *  │ BORDURE                                     │
 *  └─────────────────────────────────────────────┘
 */
public class AdminMenuGUI implements Listener {

    private final CoreEssentials plugin;
    private final Player player;
    private Inventory inv;
    private String currentPage = "main";

    // Slot constants – main page
    private static final int SLOT_PVP         = 10;
    private static final int SLOT_END         = 11;
    private static final int SLOT_NETHER      = 12;
    private static final int SLOT_WEATHER     = 13;
    private static final int SLOT_TIME        = 14;
    private static final int SLOT_ANNOUNCE    = 15;
    private static final int SLOT_CRAFTLIMIT  = 19;
    private static final int SLOT_ITEMLIMIT   = 20;
    private static final int SLOT_MACELIMIT   = 21;
    private static final int SLOT_TEAMS       = 22;
    private static final int SLOT_CMDBLOCKER  = 23;
    private static final int SLOT_SPAWNPROT   = 24;
    private static final int SLOT_RELOAD      = 40;
    private static final int SLOT_CLOSE       = 44;

    public AdminMenuGUI(CoreEssentials plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        currentPage = "main";
        buildMain();
        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ─── BUILD PAGES ──────────────────────────────────────────────────────────

    private void buildMain() {
        inv = Bukkit.createInventory(null, 54, colorize("&8⚙ &6&lAdmin Menu &8» &ePrincipal"));
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        World overworld = Bukkit.getWorlds().get(0);
        boolean pvpOn   = overworld.getPVP();

        // ── Row 1 ── World settings
        inv.setItem(SLOT_PVP, toggle(
                pvpOn ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
                "&6⚔ PvP Global",
                pvpOn,
                "&7Cliquez pour " + (pvpOn ? "&cdésactiver" : "&aactiver"),
                "&7Monde: &e" + overworld.getName()));

        boolean endEnabled = Bukkit.getWorld("world_the_end") != null
                || plugin.getConfig().getBoolean("worlds.end-enabled", true);
        inv.setItem(SLOT_END, toggle(
                Material.END_PORTAL_FRAME,
                "&dL'End",
                endEnabled,
                "&7Cliquez pour " + (endEnabled ? "&cdésactiver" : "&aactiver"),
                "&7Contrôle l'accès au End"));

        boolean netherEnabled = Bukkit.getWorld("world_nether") != null
                || plugin.getConfig().getBoolean("worlds.nether-enabled", true);
        inv.setItem(SLOT_NETHER, toggle(
                Material.NETHERRACK,
                "&cNether",
                netherEnabled,
                "&7Cliquez pour " + (netherEnabled ? "&cdésactiver" : "&aactiver"),
                "&7Contrôle l'accès au Nether"));

        // Weather
        boolean isRaining = overworld.hasStorm();
        inv.setItem(SLOT_WEATHER, makeItem(
                isRaining ? Material.WATER_BUCKET : Material.BUCKET,
                "&bMétéo",
                "&7Actuelle: " + (isRaining ? "&9Pluie/Orage" : "&eSoleil"),
                "",
                "&eClic gauche &7→ Soleil",
                "&9Clic droit &7→ Pluie",
                "&8Clic milieu &7→ Orage"));

        // Time
        long time = overworld.getTime();
        String timeStr = time < 6000 ? "Matin" : time < 12000 ? "Midi" : time < 18000 ? "Soir" : "Nuit";
        inv.setItem(SLOT_TIME, makeItem(
                Material.CLOCK,
                "&eHeure",
                "&7Actuelle: &e" + time + " &7(" + timeStr + ")",
                "",
                "&eClic gauche &7→ Jour (1000)",
                "&9Clic droit &7→ Nuit (13000)",
                "&eShift+Clic &7→ Midi (6000)"));

        // Mace announce toggle
        boolean maceAnnounce = plugin.getConfig().getBoolean("mace.announce-craft", true);
        inv.setItem(SLOT_ANNOUNCE, toggle(
                Material.BELL,
                "&6Annonces Mace",
                maceAnnounce,
                "&7Annonce craft/destruction",
                "&7de la mace en global",
                "&7Cliquez pour " + (maceAnnounce ? "&cdésactiver" : "&aactiver")));

        // ── Row 2 ── Limiter shortcuts
        boolean craftLimitOn = plugin.getCraftLimiterManager().isEnabled();
        inv.setItem(SLOT_CRAFTLIMIT, toggle(
                Material.CRAFTING_TABLE,
                "&aCraft Limiter",
                craftLimitOn,
                "&7Limite les crafts",
                "&7par joueur/item",
                "&eShift+Clic &7→ ouvrir détails",
                "&7Cliquez pour " + (craftLimitOn ? "&cdésactiver" : "&aactiver")));

        boolean itemLimitOn = plugin.getItemLimiterManager().isEnabled();
        inv.setItem(SLOT_ITEMLIMIT, toggle(
                Material.CHEST,
                "&aItem Limiter",
                itemLimitOn,
                "&7Limite les items",
                "&7dans l'inventaire",
                "&eShift+Clic &7→ ouvrir détails",
                "&7Cliquez pour " + (itemLimitOn ? "&cdésactiver" : "&aactiver")));

        boolean maceLimitOn = plugin.getMaceLimiterManager().isEnabled();
        inv.setItem(SLOT_MACELIMIT, toggle(
                Material.MACE,
                "&6Mace Limiter",
                maceLimitOn,
                "&7Limite les maces",
                "&7par joueur et par team",
                "&eShift+Clic &7→ sous-menu",
                "&7Cliquez pour " + (maceLimitOn ? "&cdésactiver" : "&aactiver")));

        inv.setItem(SLOT_TEAMS, makeItem(
                Material.ORANGE_BANNER,
                "&eTeams",
                "&7Gérer les équipes",
                "&7et leurs limitations",
                "",
                "&eClic &7→ Ouvrir le menu teams"));

        boolean cbOn = plugin.getCommandBlockerManager().isEnabled();
        inv.setItem(SLOT_CMDBLOCKER, toggle(
                Material.BARRIER,
                "&cCommand Blocker",
                cbOn,
                "&7Bloque des commandes",
                "&eShift+Clic &7→ liste des règles",
                "&7Cliquez pour " + (cbOn ? "&cdésactiver" : "&aactiver")));

        // Spawn protection
        int spawnRadius = Bukkit.getServer().getSpawnRadius();
        inv.setItem(SLOT_SPAWNPROT, makeItem(
                Material.SHIELD,
                "&aProtection Spawn",
                "&7Rayon actuel: &e" + spawnRadius + " blocs",
                "",
                "&eClic gauche &7→ +5 blocs",
                "&9Clic droit &7→ -5 blocs",
                "&eShift+Clic &7→ désactiver (0)"));

        // Reload
        inv.setItem(SLOT_RELOAD, makeItem(
                Material.NETHER_STAR,
                "&a&lRELOAD TOUT",
                "&7Recharge tous les fichiers",
                "&7de configuration",
                "",
                "&cCliquez pour confirmer"));

        // Close
        inv.setItem(SLOT_CLOSE, makeItem(
                Material.BARRIER,
                "&cFermer",
                "&7Ferme ce menu"));
    }

    private void buildMaceSubMenu() {
        inv = Bukkit.createInventory(null, 54, colorize("&8⚙ &6&lAdmin Menu &8» &eMace Limiter"));
        fillBorder(Material.ORANGE_STAINED_GLASS_PANE);

        boolean enabled = plugin.getMaceLimiterManager().isEnabled();
        int playerMax   = plugin.getMaceLimiterManager().getPerPlayerMax();
        int teamMax     = plugin.getMaceLimiterManager().getPerTeamMax();
        boolean smashDisabled = plugin.getMaceLimiterManager().isSmashDisabled();
        double maxDmg   = plugin.getMaceLimiterManager().getMaxSmashDamage();
        long smashCd    = plugin.getMaceLimiterManager().getSmashCooldownSeconds();
        boolean announceOn = plugin.getConfig().getBoolean("mace.announce-craft", true);
        boolean destroyAnnounceOn = plugin.getConfig().getBoolean("mace.announce-destroy", true);

        inv.setItem(10, toggle(Material.MACE, "&6Mace Limiter Global", enabled,
                "&7Cliquez pour " + (enabled ? "&cdésactiver" : "&aactiver")));

        inv.setItem(12, makeItem(Material.PLAYER_HEAD, "&eLimite par Joueur",
                "&7Max actuel: &e" + playerMax,
                "", "&eClic gauche &7→ +1", "&9Clic droit &7→ -1",
                "&eShift+G &7→ +5", "&eShift+D &7→ -5"));

        inv.setItem(14, makeItem(Material.ORANGE_BANNER, "&6Limite par Team",
                "&7Max actuel: &e" + teamMax,
                "", "&eClic gauche &7→ +1", "&9Clic droit &7→ -1",
                "&eShift+G &7→ +5", "&eShift+D &7→ -5"));

        inv.setItem(20, toggle(Material.FEATHER, "&cDésactiver Smash Attack", smashDisabled,
                "&7Empêche le smash attack",
                "&7Cliquez pour " + (smashDisabled ? "&aréactiver" : "&cdésactiver")));

        inv.setItem(22, makeItem(Material.DIAMOND_SWORD, "&eDégâts Max Smash",
                "&7Actuel: &e" + (maxDmg <= 0 ? "Illimité" : maxDmg),
                "", "&eClic gauche &7→ +5", "&9Clic droit &7→ -5",
                "&eShift+Clic &7→ désactiver (0)"));

        inv.setItem(24, makeItem(Material.CLOCK, "&eCooldown Smash Attack",
                "&7Actuel: &e" + smashCd + "s",
                "", "&eClic gauche &7→ +5s", "&9Clic droit &7→ -5s",
                "&eShift+Clic &7→ désactiver (0)"));

        inv.setItem(29, toggle(Material.BELL, "&6Annonce Craft Mace", announceOn,
                "&7Annonce en global quand",
                "&7une mace est craftée",
                "&7Cliquez pour " + (announceOn ? "&cdésactiver" : "&aactiver")));

        inv.setItem(31, toggle(Material.TNT, "&cAnnonce Destruction Mace", destroyAnnounceOn,
                "&7Annonce en global quand",
                "&7une mace est détruite",
                "&7Cliquez pour " + (destroyAnnounceOn ? "&cdésactiver" : "&aactiver")));

        // Back button
        inv.setItem(49, makeItem(Material.ARROW, "&7← Retour", "&7Revenir au menu principal"));
        inv.setItem(53, makeItem(Material.BARRIER, "&cFermer", ""));
    }

    private void buildTeamsMenu() {
        inv = Bukkit.createInventory(null, 54, colorize("&8⚙ &6&lAdmin Menu &8» &eTeams"));
        fillBorder(Material.YELLOW_STAINED_GLASS_PANE);

        var allTeams = plugin.getTeamManager().getAllTeams();
        int slot = 10;
        for (var team : allTeams) {
            if (slot > 43) break;
            String color = plugin.getTeamManager().formatColor(team.color);
            int maces = plugin.getTeamManager().countTeamMaces(team.name);
            inv.setItem(slot, makeItem(Material.ORANGE_BANNER,
                    color + team.displayName,
                    "&7Membres: &e" + team.members.size() + (team.maxMembers > 0 ? "/" + team.maxMembers : ""),
                    "&7Maces: &e" + maces + "/" + team.maxMaces,
                    "&7Couleur: &e" + team.color,
                    "",
                    "&eClic gauche &7→ +1 mace max",
                    "&9Clic droit &7→ -1 mace max",
                    "&eShift+Clic &7→ infos complètes"));
            slot++;
            if (slot == 18) slot = 19;
            if (slot == 27) slot = 28;
            if (slot == 36) slot = 37;
        }

        if (allTeams.isEmpty()) {
            inv.setItem(22, makeItem(Material.BARRIER,
                    "&cAucune team",
                    "&7Créez des teams avec",
                    "&f/team create <nom> <couleur>"));
        }

        inv.setItem(49, makeItem(Material.ARROW, "&7← Retour", ""));
        inv.setItem(53, makeItem(Material.BARRIER, "&cFermer", ""));
    }

    // ─── CLICK HANDLER ────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p) || !p.equals(player)) return;
        if (!isSameTitle(e)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE
                || e.getCurrentItem().getType() == Material.ORANGE_STAINED_GLASS_PANE
                || e.getCurrentItem().getType() == Material.YELLOW_STAINED_GLASS_PANE) return;

        int slot = e.getRawSlot();
        boolean shift  = e.isShiftClick();
        boolean right  = e.isRightClick();
        boolean left   = e.isLeftClick();
        boolean middle = e.getClick().name().equals("MIDDLE");

        switch (currentPage) {
            case "main"   -> handleMainClick(slot, left, right, shift, middle);
            case "mace"   -> handleMaceClick(slot, left, right, shift);
            case "teams"  -> handleTeamsClick(slot, left, right, shift);
        }
    }

    private void handleMainClick(int slot, boolean left, boolean right, boolean shift, boolean middle) {
        World world = Bukkit.getWorlds().get(0);

        switch (slot) {
            case SLOT_PVP -> {
                world.setPVP(!world.getPVP());
                String state = world.getPVP() ? "&aactivé" : "&cdésactivé";
                player.sendMessage(plugin.prefix() + "§fPvP " + colorize(state) + "§f.");
                buildMain();
                player.openInventory(inv);
            }
            case SLOT_END -> {
                boolean cur = plugin.getConfig().getBoolean("worlds.end-enabled", true);
                plugin.getConfig().set("worlds.end-enabled", !cur);
                plugin.saveConfig();
                player.sendMessage(plugin.prefix() + "§fEnd " + colorize(!cur ? "&aactivé" : "&cdésactivé") + "§f.");
                buildMain(); player.openInventory(inv);
            }
            case SLOT_NETHER -> {
                boolean cur = plugin.getConfig().getBoolean("worlds.nether-enabled", true);
                plugin.getConfig().set("worlds.nether-enabled", !cur);
                plugin.saveConfig();
                player.sendMessage(plugin.prefix() + "§fNether " + colorize(!cur ? "&aactivé" : "&cdésactivé") + "§f.");
                buildMain(); player.openInventory(inv);
            }
            case SLOT_WEATHER -> {
                if (left) {
                    world.setStorm(false); world.setThundering(false);
                    player.sendMessage(plugin.prefix() + "§fMétéo → §eSoleil");
                } else if (right) {
                    world.setStorm(true); world.setThundering(false);
                    player.sendMessage(plugin.prefix() + "§fMétéo → §9Pluie");
                } else if (middle) {
                    world.setStorm(true); world.setThundering(true);
                    player.sendMessage(plugin.prefix() + "§fMétéo → §8Orage");
                }
                buildMain(); player.openInventory(inv);
            }
            case SLOT_TIME -> {
                if (left)       world.setTime(1000);
                else if (right) world.setTime(13000);
                else if (shift) world.setTime(6000);
                player.sendMessage(plugin.prefix() + "§fHeure → §e" + world.getTime());
                buildMain(); player.openInventory(inv);
            }
            case SLOT_ANNOUNCE -> {
                boolean cur = plugin.getConfig().getBoolean("mace.announce-craft", true);
                plugin.getConfig().set("mace.announce-craft", !cur);
                plugin.saveConfig();
                player.sendMessage(plugin.prefix() + "§fAnnonces mace " + colorize(!cur ? "&aactivées" : "&cdésactivées"));
                buildMain(); player.openInventory(inv);
            }
            case SLOT_CRAFTLIMIT -> {
                if (shift) {
                    player.closeInventory();
                    player.performCommand("craftlimit list");
                } else {
                    boolean cur = plugin.getCraftLimiterManager().isEnabled();
                    plugin.getCraftLimiterManager().set("enabled", !cur);
                    player.sendMessage(plugin.prefix() + "§fCraft Limiter " + colorize(!cur ? "&aactivé" : "&cdésactivé"));
                    buildMain(); player.openInventory(inv);
                }
            }
            case SLOT_ITEMLIMIT -> {
                if (shift) {
                    player.closeInventory();
                    player.performCommand("itemlimit list");
                } else {
                    boolean cur = plugin.getItemLimiterManager().isEnabled();
                    plugin.getItemLimiterManager().set("enabled", !cur);
                    player.sendMessage(plugin.prefix() + "§fItem Limiter " + colorize(!cur ? "&aactivé" : "&cdésactivé"));
                    buildMain(); player.openInventory(inv);
                }
            }
            case SLOT_MACELIMIT -> {
                if (shift) {
                    currentPage = "mace";
                    buildMaceSubMenu();
                    player.openInventory(inv);
                } else {
                    boolean cur = plugin.getMaceLimiterManager().isEnabled();
                    plugin.getMaceLimiterManager().set("enabled", !cur);
                    player.sendMessage(plugin.prefix() + "§fMace Limiter " + colorize(!cur ? "&aactivé" : "&cdésactivé"));
                    buildMain(); player.openInventory(inv);
                }
            }
            case SLOT_TEAMS -> {
                currentPage = "teams";
                buildTeamsMenu();
                player.openInventory(inv);
            }
            case SLOT_CMDBLOCKER -> {
                if (shift) {
                    player.closeInventory();
                    player.performCommand("cb list");
                } else {
                    boolean cur = plugin.getCommandBlockerManager().isEnabled();
                    plugin.getCommandBlockerManager().set("enabled", !cur);
                    player.sendMessage(plugin.prefix() + "§fCommand Blocker " + colorize(!cur ? "&aactivé" : "&cdésactivé"));
                    buildMain(); player.openInventory(inv);
                }
            }
            case SLOT_SPAWNPROT -> {
                int cur = Bukkit.getSpawnRadius();
                if (shift)      Bukkit.setSpawnRadius(0);
                else if (left)  Bukkit.setSpawnRadius(cur + 5);
                else if (right) Bukkit.setSpawnRadius(Math.max(0, cur - 5));
                player.sendMessage(plugin.prefix() + "§fProtection spawn → §e" + Bukkit.getSpawnRadius() + " blocs");
                buildMain(); player.openInventory(inv);
            }
            case SLOT_RELOAD -> {
                player.sendMessage(plugin.prefix() + "§aRechargement en cours...");
                plugin.reloadConfig();
                plugin.getMessages().reload();
                plugin.getCommandBlockerManager().reload();
                plugin.getCustomCraftManager().reload();
                plugin.getCraftLimiterManager().reload();
                plugin.getItemLimiterManager().reload();
                plugin.getMaceLimiterManager().reload();
                plugin.getTeamManager().reload();
                player.sendMessage(plugin.prefix() + "§aTous les fichiers ont été rechargés !");
                buildMain(); player.openInventory(inv);
            }
            case SLOT_CLOSE -> player.closeInventory();
        }
    }

    private void handleMaceClick(int slot, boolean left, boolean right, boolean shift) {
        switch (slot) {
            case 10 -> { // Toggle mace limiter
                boolean cur = plugin.getMaceLimiterManager().isEnabled();
                plugin.getMaceLimiterManager().set("enabled", !cur);
                player.sendMessage(plugin.prefix() + "§fMace Limiter " + colorize(!cur ? "&aactivé" : "&cdésactivé"));
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 12 -> { // Per-player max
                int cur = plugin.getMaceLimiterManager().getPerPlayerMax();
                int delta = shift ? (left ? 5 : -5) : (left ? 1 : -1);
                int next = Math.max(0, cur + delta);
                plugin.getMaceLimiterManager().set("per-player.max", next);
                player.sendMessage(plugin.prefix() + "§fLimite joueur → §e" + next);
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 14 -> { // Per-team max
                int cur = plugin.getMaceLimiterManager().getPerTeamMax();
                int delta = shift ? (left ? 5 : -5) : (left ? 1 : -1);
                int next = Math.max(0, cur + delta);
                plugin.getMaceLimiterManager().set("per-team.max", next);
                player.sendMessage(plugin.prefix() + "§fLimite team → §e" + next);
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 20 -> { // Disable smash
                boolean cur = plugin.getMaceLimiterManager().isSmashDisabled();
                plugin.getMaceLimiterManager().set("smash-attack.disable", !cur);
                player.sendMessage(plugin.prefix() + "§fSmash attack " + colorize(!cur ? "&cdésactivé" : "&aactivé"));
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 22 -> { // Max smash damage
                double cur = plugin.getMaceLimiterManager().getMaxSmashDamage();
                double next;
                if (shift) next = 0;
                else if (left)  next = cur + 5;
                else             next = Math.max(0, cur - 5);
                plugin.getMaceLimiterManager().set("smash-attack.max-damage", next);
                player.sendMessage(plugin.prefix() + "§fDégâts max → §e" + (next <= 0 ? "Illimité" : next));
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 24 -> { // Smash cooldown
                long cur = plugin.getMaceLimiterManager().getSmashCooldownSeconds();
                long next;
                if (shift) next = 0;
                else if (left)  next = cur + 5;
                else             next = Math.max(0, cur - 5);
                plugin.getMaceLimiterManager().set("smash-attack.cooldown", next);
                player.sendMessage(plugin.prefix() + "§fCooldown smash → §e" + next + "s");
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 29 -> { // Announce craft
                boolean cur = plugin.getConfig().getBoolean("mace.announce-craft", true);
                plugin.getConfig().set("mace.announce-craft", !cur);
                plugin.saveConfig();
                player.sendMessage(plugin.prefix() + "§fAnnonce craft " + colorize(!cur ? "&aactivée" : "&cdésactivée"));
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 31 -> { // Announce destroy
                boolean cur = plugin.getConfig().getBoolean("mace.announce-destroy", true);
                plugin.getConfig().set("mace.announce-destroy", !cur);
                plugin.saveConfig();
                player.sendMessage(plugin.prefix() + "§fAnnonce destruction " + colorize(!cur ? "&aactivée" : "&cdésactivée"));
                buildMaceSubMenu(); player.openInventory(inv);
            }
            case 49 -> { currentPage = "main"; buildMain(); player.openInventory(inv); }
            case 53 -> player.closeInventory();
        }
    }

    private void handleTeamsClick(int slot, boolean left, boolean right, boolean shift) {
        if (slot == 49) { currentPage = "main"; buildMain(); player.openInventory(inv); return; }
        if (slot == 53) { player.closeInventory(); return; }

        // Find which team was clicked
        var allTeams = new ArrayList<>(plugin.getTeamManager().getAllTeams());
        int idx = getTeamIndexFromSlot(slot);
        if (idx < 0 || idx >= allTeams.size()) return;

        var team = allTeams.get(idx);
        if (shift) {
            player.closeInventory();
            player.sendMessage(plugin.getTeamManager().formatColor(team.color) + "§l" + team.displayName);
            player.sendMessage("§7Membres (" + team.members.size() + "): §e"
                    + team.members.stream()
                        .map(u -> { Player p = Bukkit.getPlayer(u); return p != null ? p.getName() : u.toString().substring(0,8); })
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b));
            player.sendMessage("§7Maces: §e" + plugin.getTeamManager().countTeamMaces(team.name) + "/" + team.maxMaces);
        } else if (left) {
            plugin.getTeamManager().setTeamMaxMaces(team.name, team.maxMaces + 1);
            player.sendMessage(plugin.prefix() + "§fMace max de §e" + team.name + " §f→ §e" + team.maxMaces);
            buildTeamsMenu(); player.openInventory(inv);
        } else if (right) {
            plugin.getTeamManager().setTeamMaxMaces(team.name, Math.max(0, team.maxMaces - 1));
            player.sendMessage(plugin.prefix() + "§fMace max de §e" + team.name + " §f→ §e" + team.maxMaces);
            buildTeamsMenu(); player.openInventory(inv);
        }
    }

    private int getTeamIndexFromSlot(int slot) {
        int[] validSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == slot) return i;
        }
        return -1;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(player)) return;
        if (!isSameTitle(e.getInventory())) return;
        HandlerList.unregisterAll(this);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private boolean isSameTitle(InventoryClickEvent e) {
        return e.getInventory().equals(inv) || e.getView().getTopInventory().equals(inv);
    }

    private boolean isSameTitle(Inventory other) {
        return other.equals(inv);
    }

    private void fillBorder(Material mat) {
        ItemStack pane = makeItem(mat, " ");
        int[] border = {0,1,2,3,4,5,6,7,8,
                        9,17, 18,26, 27,35, 36,44,
                        45,46,47,48,49,50,51,52,53};
        for (int s : border) inv.setItem(s, pane);
    }

    private ItemStack toggle(Material mat, String name, boolean state, String... lore) {
        List<String> l = new ArrayList<>();
        l.add(state ? colorize("&a● ACTIVÉ") : colorize("&c● DÉSACTIVÉ"));
        l.add("");
        for (String line : lore) l.add(colorize(line));
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(name));
        meta.setLore(l);
        // Glint when enabled
        if (state) meta.addEnchant(
                org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
                          org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(name));
        List<String> l = new ArrayList<>();
        for (String line : lore) l.add(colorize(line));
        if (!l.isEmpty()) meta.setLore(l);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
                          org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    /** Needed because CraftLimiterManager/ItemLimiterManager need a set("enabled",...) method */
    private void set(String path, Object value) {}

    private String colorize(String s) { return CoreEssentials.colorize(s); }
}
