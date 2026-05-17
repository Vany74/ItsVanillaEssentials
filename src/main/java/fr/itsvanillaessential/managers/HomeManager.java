package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final CoreEssentials plugin;
    private final File homesFile;
    private FileConfiguration homesConfig;

    // uuid -> (homeName -> location)
    private final Map<UUID, Map<String, Location>> homes = new HashMap<>();

    public HomeManager(CoreEssentials plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try { homesFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        loadHomes();
    }

    private void loadHomes() {
        if (homesConfig.getConfigurationSection("homes") == null) return;
        for (String uuidStr : homesConfig.getConfigurationSection("homes").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Location> playerHomes = new HashMap<>();
            var section = homesConfig.getConfigurationSection("homes." + uuidStr);
            if (section == null) continue;
            for (String homeName : section.getKeys(false)) {
                String path = "homes." + uuidStr + "." + homeName;
                String worldName = homesConfig.getString(path + ".world");
                if (worldName == null || Bukkit.getWorld(worldName) == null) continue;
                double x = homesConfig.getDouble(path + ".x");
                double y = homesConfig.getDouble(path + ".y");
                double z = homesConfig.getDouble(path + ".z");
                float yaw   = (float) homesConfig.getDouble(path + ".yaw");
                float pitch = (float) homesConfig.getDouble(path + ".pitch");
                playerHomes.put(homeName.toLowerCase(), new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
            }
            homes.put(uuid, playerHomes);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Map<String, Location>> e : homes.entrySet()) {
            for (Map.Entry<String, Location> h : e.getValue().entrySet()) {
                String path = "homes." + e.getKey() + "." + h.getKey();
                Location loc = h.getValue();
                homesConfig.set(path + ".world", loc.getWorld().getName());
                homesConfig.set(path + ".x", loc.getX());
                homesConfig.set(path + ".y", loc.getY());
                homesConfig.set(path + ".z", loc.getZ());
                homesConfig.set(path + ".yaw", loc.getYaw());
                homesConfig.set(path + ".pitch", loc.getPitch());
            }
        }
        try { homesConfig.save(homesFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<String, Location> getHomes(UUID uuid) {
        return homes.getOrDefault(uuid, new HashMap<>());
    }

    public Location getHome(UUID uuid, String name) {
        return homes.getOrDefault(uuid, new HashMap<>()).get(name.toLowerCase());
    }

    public void setHome(UUID uuid, String name, Location loc) {
        homes.computeIfAbsent(uuid, k -> new HashMap<>()).put(name.toLowerCase(), loc);
        saveAll();
    }

    public boolean deleteHome(UUID uuid, String name) {
        Map<String, Location> m = homes.get(uuid);
        if (m == null || !m.containsKey(name.toLowerCase())) return false;
        m.remove(name.toLowerCase());
        homesConfig.set("homes." + uuid + "." + name.toLowerCase(), null);
        try { homesConfig.save(homesFile); } catch (IOException e) { e.printStackTrace(); }
        return true;
    }

    public int getMaxHomes(org.bukkit.entity.Player player) {
        if (player.hasPermission("core.home.premium")) return plugin.getConfig().getInt("homes.max-homes-premium", 10);
        if (player.hasPermission("core.home.vip"))     return plugin.getConfig().getInt("homes.max-homes-vip", 6);
        return plugin.getConfig().getInt("homes.max-homes-default", 3);
    }
}
