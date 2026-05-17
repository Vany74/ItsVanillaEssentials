package fr.itsvanillaessential.gui;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI Layout (9×6 = 54 slots):
 *
 *  [ 0][ 1][ 2]  [ 3]  [ 4][ 5][ 6][ 7][ 8]
 *  [ 9][10][11]  [12]  [13][14][15][16][17]
 *  [18][19][20]  [21]  [22][23][24][25][26]
 *  [27][28][29][30][31][32][33][34][35]
 *  [36][37][38][39][40][41][42][43][44]
 *  [45][46][47][48][49][50][51][52][53]
 *
 *  Slots 0-2 / 9-11 / 18-20 = Grille de craft (3x3)
 *  Slot  24  = Résultat
 *  Slot  49  = Bouton SAUVEGARDER (tête verte)
 *  Slot  45  = Bouton ANNULER (tête rouge)
 *  Reste     = Gris de remplissage
 */
public class CraftEditorGUI implements Listener {

    private static final int[] GRID_SLOTS   = {0, 1, 2, 9, 10, 11, 18, 19, 20};
    private static final int   RESULT_SLOT  = 24;
    private static final int   SAVE_SLOT    = 49;
    private static final int   CANCEL_SLOT  = 45;

    private final CoreEssentials plugin;
    private final Player player;
    private final String craftId;
    private Inventory inv;

    public CraftEditorGUI(CoreEssentials plugin, Player player, String craftId) {
        this.plugin   = plugin;
        this.player   = player;
        this.craftId  = craftId;
    }

    public void open() {
        inv = Bukkit.createInventory(null, 54,
                CoreEssentials.colorize("&8Craft Editor &7» &e" + craftId));

        // Fill background
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);

        // Grid cells → air (clickable)
        for (int s : GRID_SLOTS) inv.setItem(s, makeItem(Material.AIR, " "));

        // Result cell
        inv.setItem(RESULT_SLOT, makeItem(Material.AIR, " "));

        // Arrow decoration
        inv.setItem(22, makeItem(Material.ARROW, "§7Résultat →"));

        // Save button
        inv.setItem(SAVE_SLOT, makeItem(Material.LIME_WOOL,
                "§a§lSAUVEGARDER",
                "§7Cliquez pour enregistrer",
                "§7le craft §e" + craftId));

        // Cancel button
        inv.setItem(CANCEL_SLOT, makeItem(Material.RED_WOOL,
                "§c§lANNULER",
                "§7Ferme sans sauvegarder."));

        // Info panel (right side)
        inv.setItem(6,  makeItem(Material.BOOK, "§6§lComment utiliser",
                "§71. Placez vos ingrédients",
                "§7   dans la grille §e(gauche)§7.",
                "§72. Placez le résultat",
                "§7   dans le slot §e(droite)§7.",
                "§73. Cliquez §aSAUVEGARDER§7.",
                "",
                "§7Laissez une case vide",
                "§7pour ignorer la position."));

        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.equals(player)) return;
        if (!e.getView().title().equals(
                net.kyori.adventure.text.Component.text(
                        CoreEssentials.colorize("&8Craft Editor &7» &e" + craftId)))) return;

        int slot = e.getRawSlot();

        // Allow placing items in grid and result slot
        if (isGridSlot(slot) || slot == RESULT_SLOT) {
            // Let default behavior happen (player places/takes items)
            return;
        }

        e.setCancelled(true);

        if (slot == SAVE_SLOT) {
            saveCraft();
        } else if (slot == CANCEL_SLOT) {
            returnItems();
            p.closeInventory();
            p.sendMessage(plugin.prefix() + "§cCréation du craft annulée.");
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(player)) return;
        if (!isOurInventory(e.getInventory())) return;
        HandlerList.unregisterAll(this);
    }

    private void saveCraft() {
        // Read grid
        ItemStack[] gridItems = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack it = inv.getItem(GRID_SLOTS[i]);
            gridItems[i] = (it == null || it.getType().isAir()) ? null : it;
        }

        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result == null || result.getType().isAir()) {
            player.sendMessage(plugin.prefix() + "§cVous devez placer un item §erésultat§c.");
            return;
        }

        // Check at least one ingredient
        boolean hasIngredient = false;
        for (ItemStack gi : gridItems) {
            if (gi != null) { hasIngredient = true; break; }
        }
        if (!hasIngredient) {
            player.sendMessage(plugin.prefix() + "§cVous devez placer au moins un ingrédient.");
            return;
        }

        // Build shape rows and ingredient map
        // Assign a letter to each unique material
        Map<Material, Character> matToChar = new LinkedHashMap<>();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        int letterIdx = 0;

        StringBuilder row1 = new StringBuilder();
        StringBuilder row2 = new StringBuilder();
        StringBuilder row3 = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            ItemStack gi = gridItems[i];
            char c;
            if (gi == null || gi.getType().isAir()) {
                c = ' ';
            } else {
                Material mat = gi.getType();
                if (!matToChar.containsKey(mat)) {
                    if (letterIdx >= letters.length) {
                        player.sendMessage(plugin.prefix() + "§cTrop d'ingrédients différents (max " + letters.length + ").");
                        return;
                    }
                    matToChar.put(mat, letters[letterIdx++]);
                }
                c = matToChar.get(mat);
            }
            if (i < 3) row1.append(c);
            else if (i < 6) row2.append(c);
            else row3.append(c);
        }

        Map<Character, Material> ingredients = new LinkedHashMap<>();
        for (Map.Entry<Material, Character> e : matToChar.entrySet()) {
            ingredients.put(e.getValue(), e.getKey());
        }

        // Save via manager
        try {
            String savedId = plugin.getCustomCraftManager().addShapedCraft(
                    craftId,
                    result.getType(),
                    result.getAmount(),
                    row1.toString(),
                    row2.toString(),
                    row3.toString(),
                    ingredients
            );

            // Return items to player
            returnItems();
            player.closeInventory();

            player.sendMessage(plugin.prefix() + "§aCraft §e" + savedId + " §acréé avec succès !");
            player.sendMessage("§7Shape: §f" + row1 + " §8/ §f" + row2 + " §8/ §f" + row3);
            player.sendMessage("§7Résultat: §f" + result.getType().name() + " §7x" + result.getAmount());

        } catch (Exception ex) {
            player.sendMessage(plugin.prefix() + "§cErreur lors de la création: §e" + ex.getMessage());
            plugin.getLogger().warning("[CraftGUI] Erreur: " + ex.getMessage());
        }
    }

    /** Return all items placed in the GUI back to the player */
    private void returnItems() {
        for (int s : GRID_SLOTS) {
            ItemStack it = inv.getItem(s);
            if (it != null && !it.getType().isAir()) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(it);
                leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
            }
        }
        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result != null && !result.getType().isAir()) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(result);
            leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
        }
    }

    private boolean isGridSlot(int slot) {
        for (int s : GRID_SLOTS) if (s == slot) return true;
        return false;
    }

    private boolean isOurInventory(Inventory other) {
        return other.equals(inv);
    }

    // ─── Item builder ──────────────────────────────────────────────────────────

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(CoreEssentials.colorize(name));
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String l : lore) loreList.add(CoreEssentials.colorize(l));
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }
}
