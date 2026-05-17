package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final CoreEssentials plugin;
    private final File dataFile;
    private FileConfiguration data;

    // Runtime caches
    private final Map<UUID, Location> backLocations   = new HashMap<>();
    private final Map<UUID, String>   lastMessaged     = new HashMap<>();
    private final Map<UUID, String>   nicknames        = new HashMap<>();
    private final Set<UUID>           afkPlayers       = new HashSet<>();
    private final Set<UUID>           godPlayers       = new HashSet<>();
    private final Set<UUID>           socialSpyPlayers = new HashSet<>();

    public PlayerDataManager(CoreEssentials plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        loadNicknames();
    }

    private void loadNicknames() {
        if (data.getConfigurationSection("nicks") == null) return;
        for (String key : data.getConfigurationSection("nicks").getKeys(false)) {
            nicknames.put(UUID.fromString(key), data.getString("nicks." + key));
        }
    }

    public void saveAll() {
        // Save nicks
        for (Map.Entry<UUID, String> e : nicknames.entrySet()) {
            data.set("nicks." + e.getKey(), e.getValue());
        }
        // Save last seen
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveLastSeen(Player player) {
        data.set("lastseen." + player.getUniqueId(), System.currentTimeMillis());
        data.set("lastname." + player.getUniqueId(), player.getName());
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public long getLastSeen(String name) {
        for (String key : Objects.requireNonNull(data.getConfigurationSection("lastname") != null
                ? data.getConfigurationSection("lastname") : data.createSection("lastname")).getKeys(false)) {
            if (data.getString("lastname." + key, "").equalsIgnoreCase(name)) {
                return data.getLong("lastseen." + key, -1L);
            }
        }
        return -1L;
    }

    // Back
    public void setBackLocation(UUID uuid, Location loc) { backLocations.put(uuid, loc); }
    public Location getBackLocation(UUID uuid)            { return backLocations.get(uuid); }

    // Last messaged
    public void setLastMessaged(UUID uuid, String target) { lastMessaged.put(uuid, target); }
    public String getLastMessaged(UUID uuid)               { return lastMessaged.get(uuid); }

    // Nickname
    public void setNick(UUID uuid, String nick)  { nicknames.put(uuid, nick); }
    public void removeNick(UUID uuid)             { nicknames.remove(uuid); }
    public String getNick(UUID uuid)              { return nicknames.getOrDefault(uuid, null); }

    // AFK
    public void setAfk(UUID uuid, boolean afk) {
        if (afk) afkPlayers.add(uuid); else afkPlayers.remove(uuid);
    }
    public boolean isAfk(UUID uuid) { return afkPlayers.contains(uuid); }

    // God mode
    public void setGod(UUID uuid, boolean god) {
        if (god) godPlayers.add(uuid); else godPlayers.remove(uuid);
    }
    public boolean isGod(UUID uuid) { return godPlayers.contains(uuid); }

    // Social spy
    public void toggleSocialSpy(UUID uuid) {
        if (socialSpyPlayers.contains(uuid)) socialSpyPlayers.remove(uuid);
        else socialSpyPlayers.add(uuid);
    }
    public boolean hasSocialSpy(UUID uuid) { return socialSpyPlayers.contains(uuid); }
    public Set<UUID> getSocialSpyPlayers()  { return socialSpyPlayers; }
}
