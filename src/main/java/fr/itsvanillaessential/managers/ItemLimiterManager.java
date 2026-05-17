package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemLimiterManager {

    private final CoreEssentials plugin;
    private File configFile;
    private FileConfiguration config;

    public ItemLimiterManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "itemlimiter.yml");
        if (!configFile.exists()) plugin.saveResource("itemlimiter.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }

    public ConfigurationSection getLimit(Material mat) {
        return config.getConfigurationSection("limits." + mat.name());
    }

    public boolean hasLimit(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null && s.getBoolean("enabled", true);
    }

    public String getBypassPermission(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getString("bypass-permission", "") : "";
    }

    public String getAction(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getString("action", "deny") : "deny";
    }

    public String getBlockedMessage(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return "";
        String msg = s.getString("blocked-message", "");
        if (msg.isEmpty()) {
            msg = config.getString("default-blocked-message",
                    "&cVous ne pouvez pas avoir plus de {max} {item}.");
        }
        ConfigurationSection modes = s.getConfigurationSection("modes");
        int maxInv = modes != null ? modes.getInt("inventory", 0) : 0;
        return msg.replace("{max}", String.valueOf(maxInv))
                  .replace("{item}", mat.name().toLowerCase().replace("_", " "));
    }

    // ─── Mode checks ──────────────────────────────────────────────────────────

    public int getInventoryMax(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return -1;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        if (modes == null || !modes.contains("inventory")) return -1;
        return modes.getInt("inventory", -1);
    }

    public int getHotbarMax(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return -1;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        if (modes == null || !modes.contains("hotbar")) return -1;
        return modes.getInt("hotbar", -1);
    }

    public boolean isHeldBlocked(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return false;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        return modes != null && modes.getBoolean("held", false);
    }

    public boolean isPickupBlocked(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return false;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        return modes != null && modes.getBoolean("pickup", false);
    }

    public boolean isDropBlocked(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return false;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        return modes != null && modes.getBoolean("drop", false);
    }

    public boolean isUseBlocked(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return false;
        ConfigurationSection modes = s.getConfigurationSection("modes");
        return modes != null && modes.getBoolean("use", false);
    }

    // ─── Count items in inventory ─────────────────────────────────────────────

    public int countInInventory(Player player, Material mat) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) total += item.getAmount();
        }
        return total;
    }

    public int countInHotbar(Player player, Material mat) {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == mat) total += item.getAmount();
        }
        return total;
    }

    // ─── Main check ───────────────────────────────────────────────────────────

    public CheckResult checkInventory(Player player, Material mat) {
        if (!isEnabled() || !hasLimit(mat)) return CheckResult.ALLOWED;
        String bypass = getBypassPermission(mat);
        if (!bypass.isEmpty() && player.hasPermission(bypass)) return CheckResult.ALLOWED;

        int maxInv = getInventoryMax(mat);
        if (maxInv >= 0 && countInInventory(player, mat) >= maxInv) return CheckResult.INVENTORY_EXCEEDED;

        int maxHotbar = getHotbarMax(mat);
        if (maxHotbar >= 0 && countInHotbar(player, mat) >= maxHotbar) return CheckResult.HOTBAR_EXCEEDED;

        return CheckResult.ALLOWED;
    }

    // ─── Config write ─────────────────────────────────────────────────────────

    public void setLimit(Material mat, String mode, String value) {
        String path = "limits." + mat.name();
        config.set(path + ".enabled", true);
        if (mode.equalsIgnoreCase("action")) {
            config.set(path + ".action", value);
        } else if (mode.equalsIgnoreCase("bypass")) {
            config.set(path + ".bypass-permission", value);
        } else if (mode.equalsIgnoreCase("message")) {
            config.set(path + ".blocked-message", value);
        } else {
            try {
                int intVal = Integer.parseInt(value);
                config.set(path + ".modes." + mode, intVal);
            } catch (NumberFormatException e) {
                config.set(path + ".modes." + mode, Boolean.parseBoolean(value));
            }
        }
        saveConfig();
    }

    public void removeLimit(Material mat) {
        config.set("limits." + mat.name(), null);
        saveConfig();
    }

    public Set<String> getLimitedMaterials() {
        ConfigurationSection s = config.getConfigurationSection("limits");
        return s != null ? s.getKeys(false) : new HashSet<>();
    }

    private void saveConfig() {
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public enum CheckResult { ALLOWED, INVENTORY_EXCEEDED, HOTBAR_EXCEEDED }

    /** Generic config setter used by AdminMenuGUI */
    public void set(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

}
