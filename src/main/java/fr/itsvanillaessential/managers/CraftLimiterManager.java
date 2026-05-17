package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CraftLimiterManager {

    private final CoreEssentials plugin;
    private File configFile;
    private FileConfiguration config;

    // UUID -> (Material -> count)
    private File dataFile;
    private FileConfiguration data;

    // UUID -> (Material -> last craft timestamp)
    private final Map<UUID, Map<Material, Long>> cooldowns = new HashMap<>();

    public CraftLimiterManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "craftlimiter.yml");
        if (!configFile.exists()) plugin.saveResource("craftlimiter.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);

        dataFile = new File(plugin.getDataFolder(), "craftlimiter_data.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }

    // ─── Limit lookup ─────────────────────────────────────────────────────────

    public ConfigurationSection getLimit(Material mat) {
        return config.getConfigurationSection("limits." + mat.name());
    }

    public boolean hasLimit(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null && s.getBoolean("enabled", true);
    }

    public int getMax(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getInt("max-per-player", 1) : 1;
    }

    public String getPermission(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getString("permission", "") : "";
    }

    public String getBypassPermission(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getString("bypass-permission", "") : "";
    }

    public long getCooldownSeconds(Material mat) {
        ConfigurationSection s = getLimit(mat);
        return s != null ? s.getLong("cooldown", 0) : 0;
    }

    public String getBlockedMessage(Material mat) {
        ConfigurationSection s = getLimit(mat);
        if (s == null) return "";
        String msg = s.getString("blocked-message", "");
        return msg.isEmpty() ? config.getString("default-blocked-message",
                "&cVous ne pouvez pas crafter cet item.") : msg;
    }

    // ─── Player craft counts ──────────────────────────────────────────────────

    public int getCraftCount(UUID uuid, Material mat) {
        return data.getInt(uuid + "." + mat.name(), 0);
    }

    public void incrementCount(UUID uuid, Material mat) {
        int current = getCraftCount(uuid, mat);
        data.set(uuid + "." + mat.name(), current + 1);
        saveData();
    }

    public void resetCount(UUID uuid, Material mat) {
        data.set(uuid + "." + mat.name(), 0);
        saveData();
    }

    public void resetAllCounts(UUID uuid) {
        data.set(uuid.toString(), null);
        saveData();
    }

    // ─── Cooldown ─────────────────────────────────────────────────────────────

    public boolean isOnCooldown(UUID uuid, Material mat) {
        long cd = getCooldownSeconds(mat);
        if (cd <= 0) return false;
        Map<Material, Long> map = cooldowns.getOrDefault(uuid, new HashMap<>());
        Long last = map.get(mat);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < cd * 1000L;
    }

    public long getRemainingCooldown(UUID uuid, Material mat) {
        Map<Material, Long> map = cooldowns.getOrDefault(uuid, new HashMap<>());
        Long last = map.get(mat);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        return getCooldownSeconds(mat) - elapsed;
    }

    public void setCooldown(UUID uuid, Material mat) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(mat, System.currentTimeMillis());
    }

    // ─── Check ────────────────────────────────────────────────────────────────

    public CheckResult canCraft(Player player, Material mat) {
        if (!isEnabled() || !hasLimit(mat)) return CheckResult.ALLOWED;

        // Bypass
        String bypass = getBypassPermission(mat);
        if (!bypass.isEmpty() && player.hasPermission(bypass)) return CheckResult.ALLOWED;

        // Permission to craft
        String perm = getPermission(mat);
        if (!perm.isEmpty() && !player.hasPermission(perm)) return CheckResult.NO_PERMISSION;

        // Cooldown
        if (isOnCooldown(player.getUniqueId(), mat)) return CheckResult.COOLDOWN;

        // Max count
        int max   = getMax(mat);
        int count = getCraftCount(player.getUniqueId(), mat);
        if (count >= max) return CheckResult.LIMIT_REACHED;

        return CheckResult.ALLOWED;
    }

    public enum CheckResult { ALLOWED, LIMIT_REACHED, NO_PERMISSION, COOLDOWN }

    // ─── Config manipulation ──────────────────────────────────────────────────

    public void setLimit(Material mat, String option, String value) {
        String path = "limits." + mat.name();
        config.set(path + ".enabled", true);
        switch (option.toLowerCase()) {
            case "max"      -> config.set(path + ".max-per-player", Integer.parseInt(value));
            case "cooldown" -> config.set(path + ".cooldown", Long.parseLong(value));
            case "message"  -> config.set(path + ".blocked-message", value);
            case "bypass"   -> config.set(path + ".bypass-permission", value);
            case "enabled"  -> config.set(path + ".enabled", Boolean.parseBoolean(value));
            default         -> config.set(path + "." + option, value);
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

    private void saveData() {
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    /** Generic config setter used by AdminMenuGUI */
    public void set(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

}
