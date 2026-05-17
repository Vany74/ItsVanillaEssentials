package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomCraftManager {

    private final CoreEssentials plugin;
    private File craftsFile;
    private FileConfiguration craftsConfig;

    // key -> recipe key (for unregistering)
    private final Map<String, NamespacedKey> registeredKeys = new LinkedHashMap<>();

    public CustomCraftManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    // ─── Load / Reload ────────────────────────────────────────────────────────

    public void reload() {
        craftsFile = new File(plugin.getDataFolder(), "customcrafts.yml");
        if (!craftsFile.exists()) {
            plugin.saveResource("customcrafts.yml", false);
        }
        craftsConfig = YamlConfiguration.loadConfiguration(craftsFile);
        unregisterAll();
        registerAll();
    }

    private void registerAll() {
        ConfigurationSection section = craftsConfig.getConfigurationSection("crafts");
        if (section == null) return;
        int loaded = 0;
        for (String id : section.getKeys(false)) {
            try {
                registerRecipe(id, section.getConfigurationSection(id));
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("[CustomCraft] Erreur chargement craft '" + id + "': " + e.getMessage());
            }
        }
        plugin.getLogger().info("[CustomCraft] " + loaded + " craft(s) personnalisé(s) chargé(s).");
    }

    private void registerRecipe(String id, ConfigurationSection cfg) {
        if (cfg == null) return;

        String type    = cfg.getString("type", "shaped").toLowerCase();
        ItemStack result = buildItem(cfg.getConfigurationSection("result"));
        if (result == null) throw new IllegalArgumentException("Résultat invalide");

        NamespacedKey key = new NamespacedKey(plugin, "craft_" + id.toLowerCase());

        switch (type) {
            case "shaped"   -> registerShaped(key, cfg, result);
            case "shapeless"-> registerShapeless(key, cfg, result);
            default -> throw new IllegalArgumentException("Type inconnu: " + type);
        }

        registeredKeys.put(id.toLowerCase(), key);
    }

    private void registerShaped(NamespacedKey key, ConfigurationSection cfg, ItemStack result) {
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        // Shape: liste de 3 lignes max (ex: ["AAA","ABA","AAA"])
        List<String> shape = cfg.getStringList("shape");
        if (shape.isEmpty() || shape.size() > 3) throw new IllegalArgumentException("Shape invalide (1-3 lignes)");
        recipe.shape(shape.toArray(new String[0]));

        // Ingredients: map de char -> material
        ConfigurationSection ing = cfg.getConfigurationSection("ingredients");
        if (ing != null) {
            for (String charKey : ing.getKeys(false)) {
                char c = charKey.charAt(0);
                String matName = ing.getString(charKey, "AIR");
                Material mat = Material.matchMaterial(matName);
                if (mat == null) throw new IllegalArgumentException("Matériau inconnu: " + matName);
                recipe.setIngredient(c, mat);
            }
        }
        plugin.getServer().addRecipe(recipe);
    }

    private void registerShapeless(NamespacedKey key, ConfigurationSection cfg, ItemStack result) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        List<String> ingredients = cfg.getStringList("ingredients");
        for (String matName : ingredients) {
            Material mat = Material.matchMaterial(matName);
            if (mat == null) throw new IllegalArgumentException("Matériau inconnu: " + matName);
            recipe.addIngredient(mat);
        }
        plugin.getServer().addRecipe(recipe);
    }

    // ─── Build result ItemStack ────────────────────────────────────────────────

    public ItemStack buildItem(ConfigurationSection cfg) {
        if (cfg == null) return null;
        String matName = cfg.getString("material", "AIR");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) return null;

        int amount = cfg.getInt("amount", 1);
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Name
        String name = cfg.getString("name", "");
        if (!name.isEmpty()) meta.setDisplayName(CoreEssentials.colorize(name));

        // Lore
        List<String> lore = cfg.getStringList("lore");
        if (!lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            lore.forEach(l -> coloredLore.add(CoreEssentials.colorize(l)));
            meta.setLore(coloredLore);
        }

        // Enchantments
        ConfigurationSection enchSection = cfg.getConfigurationSection("enchantments");
        if (enchSection != null) {
            for (String enchName : enchSection.getKeys(false)) {
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(enchName.toLowerCase()));
                if (ench != null) {
                    int level = enchSection.getInt(enchName, 1);
                    meta.addEnchant(ench, level, true);
                }
            }
        }

        // Custom model data
        if (cfg.contains("custom-model-data")) {
            meta.setCustomModelData(cfg.getInt("custom-model-data"));
        }

        item.setItemMeta(meta);
        return item;
    }

    // ─── Unregister ───────────────────────────────────────────────────────────

    public void unregisterAll() {
        for (NamespacedKey key : registeredKeys.values()) {
            plugin.getServer().removeRecipe(key);
        }
        registeredKeys.clear();
    }

    public boolean unregister(String id) {
        NamespacedKey key = registeredKeys.remove(id.toLowerCase());
        if (key == null) return false;
        plugin.getServer().removeRecipe(key);
        return true;
    }

    // ─── Add / Remove / Save ──────────────────────────────────────────────────

    /**
     * Add a shaped craft via command:
     * /addcraft shaped <id> <result_material> <amount> <row1> <row2> <row3> <A:MATERIAL> [B:MATERIAL] ...
     */
    public String addShapedCraft(String id, Material resultMat, int amount,
                                  String row1, String row2, String row3,
                                  Map<Character, Material> ingredients) {
        String safeId = id.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String path   = "crafts." + safeId;

        craftsConfig.set(path + ".type", "shaped");
        craftsConfig.set(path + ".shape", List.of(row1, row2, row3));
        craftsConfig.set(path + ".result.material", resultMat.name());
        craftsConfig.set(path + ".result.amount", amount);
        for (Map.Entry<Character, Material> e : ingredients.entrySet()) {
            craftsConfig.set(path + ".ingredients." + e.getKey(), e.getValue().name());
        }
        save();

        // Register immediately
        ConfigurationSection cfg = craftsConfig.getConfigurationSection(path);
        ItemStack result = buildItem(cfg.getConfigurationSection("result"));
        NamespacedKey key = new NamespacedKey(plugin, "craft_" + safeId);
        registerShaped(key, cfg, result);
        registeredKeys.put(safeId, key);
        return safeId;
    }

    public String addShapelessCraft(String id, Material resultMat, int amount, List<Material> ingredients) {
        String safeId = id.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String path   = "crafts." + safeId;

        craftsConfig.set(path + ".type", "shapeless");
        craftsConfig.set(path + ".result.material", resultMat.name());
        craftsConfig.set(path + ".result.amount", amount);
        List<String> ingList = new ArrayList<>();
        ingredients.forEach(m -> ingList.add(m.name()));
        craftsConfig.set(path + ".ingredients", ingList);
        save();

        ConfigurationSection cfg = craftsConfig.getConfigurationSection(path);
        ItemStack result = buildItem(cfg.getConfigurationSection("result"));
        NamespacedKey key = new NamespacedKey(plugin, "craft_" + safeId);
        registerShapeless(key, cfg, result);
        registeredKeys.put(safeId, key);
        return safeId;
    }

    public boolean deleteCraft(String id) {
        String safeId = id.toLowerCase();
        if (!registeredKeys.containsKey(safeId)) return false;
        unregister(safeId);
        craftsConfig.set("crafts." + safeId, null);
        save();
        return true;
    }

    public Set<String> getCraftIds() { return registeredKeys.keySet(); }

    public ConfigurationSection getCraftSection(String id) {
        return craftsConfig.getConfigurationSection("crafts." + id.toLowerCase());
    }

    private void save() {
        try { craftsConfig.save(craftsFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
