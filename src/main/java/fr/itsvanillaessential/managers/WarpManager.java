package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpManager {

    private final CoreEssentials plugin;
    private final File warpsFile;
    private FileConfiguration warpsConfig;
    private final Map<String, Location> warps = new HashMap<>();

    public WarpManager(CoreEssentials plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            try { warpsFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        loadWarps();
    }

    private void loadWarps() {
        if (warpsConfig.getConfigurationSection("warps") == null) return;
        for (String name : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
            String path = "warps." + name;
            String worldName = warpsConfig.getString(path + ".world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue;
            double x = warpsConfig.getDouble(path + ".x");
            double y = warpsConfig.getDouble(path + ".y");
            double z = warpsConfig.getDouble(path + ".z");
            float yaw   = (float) warpsConfig.getDouble(path + ".yaw");
            float pitch = (float) warpsConfig.getDouble(path + ".pitch");
            warps.put(name.toLowerCase(), new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
        }
    }

    private void save() {
        try { warpsConfig.save(warpsFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public Location getWarp(String name) { return warps.get(name.toLowerCase()); }

    public Set<String> getWarpNames() { return warps.keySet(); }

    public void setWarp(String name, Location loc) {
        String n = name.toLowerCase();
        warps.put(n, loc);
        String path = "warps." + n;
        warpsConfig.set(path + ".world", loc.getWorld().getName());
        warpsConfig.set(path + ".x", loc.getX());
        warpsConfig.set(path + ".y", loc.getY());
        warpsConfig.set(path + ".z", loc.getZ());
        warpsConfig.set(path + ".yaw", loc.getYaw());
        warpsConfig.set(path + ".pitch", loc.getPitch());
        save();
    }

    public boolean deleteWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) return false;
        warps.remove(name.toLowerCase());
        warpsConfig.set("warps." + name.toLowerCase(), null);
        save();
        return true;
    }
}
