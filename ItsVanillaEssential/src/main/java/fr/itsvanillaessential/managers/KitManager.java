package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {

    private final CoreEssentials plugin;
    private final File cooldownFile;
    private FileConfiguration cooldownData;

    public KitManager(CoreEssentials plugin) {
        this.plugin = plugin;
        this.cooldownFile = new File(plugin.getDataFolder(), "kit_cooldowns.yml");
        if (!cooldownFile.exists()) {
            try { cooldownFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.cooldownData = YamlConfiguration.loadConfiguration(cooldownFile);
    }

    public Set<String> getKitNames() {
        var section = plugin.getConfig().getConfigurationSection("kits");
        return section != null ? section.getKeys(false) : new HashSet<>();
    }

    public List<ItemStack> getKitItems(String kitName) {
        List<ItemStack> items = new ArrayList<>();
        List<String> raw = plugin.getConfig().getStringList("kits." + kitName + ".items");
        for (String entry : raw) {
            String[] parts = entry.split(":");
            Material mat = Material.matchMaterial(parts[0]);
            if (mat == null) continue;
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            items.add(new ItemStack(mat, amount));
        }
        return items;
    }

    public long getCooldown(String kitName) {
        return plugin.getConfig().getLong("kits." + kitName + ".cooldown", 0L);
    }

    public boolean isOnCooldown(Player player, String kitName) {
        long cooldownSecs = getCooldown(kitName);
        if (cooldownSecs == 0) return false;
        long last = cooldownData.getLong(player.getUniqueId() + "." + kitName, 0L);
        if (last == 0) return false;
        if (cooldownSecs == -1) return true; // one-time kit
        return (System.currentTimeMillis() - last) < cooldownSecs * 1000L;
    }

    public long getRemainingCooldown(Player player, String kitName) {
        long last = cooldownData.getLong(player.getUniqueId() + "." + kitName, 0L);
        long cooldownSecs = getCooldown(kitName);
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        return cooldownSecs - elapsed;
    }

    public void setCooldown(Player player, String kitName) {
        cooldownData.set(player.getUniqueId() + "." + kitName, System.currentTimeMillis());
        try { cooldownData.save(cooldownFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void giveKit(Player player, String kitName) {
        List<ItemStack> items = getKitItems(kitName);
        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
        }
        setCooldown(player, kitName);
    }
}
