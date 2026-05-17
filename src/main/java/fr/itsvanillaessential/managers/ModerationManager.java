package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ModerationManager {

    private final CoreEssentials plugin;
    private final File modFile;
    private FileConfiguration modData;

    // uuid -> mute expiry (ms), -1 = permanent
    private final Map<UUID, Long> mutes = new HashMap<>();

    public ModerationManager(CoreEssentials plugin) {
        this.plugin = plugin;
        this.modFile = new File(plugin.getDataFolder(), "moderation.yml");
        if (!modFile.exists()) {
            try { modFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.modData = YamlConfiguration.loadConfiguration(modFile);
        loadMutes();
    }

    private void loadMutes() {
        var section = modData.getConfigurationSection("mutes");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            mutes.put(UUID.fromString(key), modData.getLong("mutes." + key));
        }
    }

    private void save() {
        try { modData.save(modFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── MUTE ────────────────────────────────────────────────────────────────

    public void mute(UUID uuid, long durationMs) {
        long expiry = durationMs <= 0 ? -1L : System.currentTimeMillis() + durationMs;
        mutes.put(uuid, expiry);
        modData.set("mutes." + uuid, expiry);
        save();
    }

    public void unmute(UUID uuid) {
        mutes.remove(uuid);
        modData.set("mutes." + uuid, null);
        save();
    }

    public boolean isMuted(UUID uuid) {
        if (!mutes.containsKey(uuid)) return false;
        long expiry = mutes.get(uuid);
        if (expiry == -1) return true;
        if (System.currentTimeMillis() > expiry) {
            unmute(uuid);
            return false;
        }
        return true;
    }

    public long getMuteExpiry(UUID uuid) { return mutes.getOrDefault(uuid, 0L); }

    // ─── WARNINGS ────────────────────────────────────────────────────────────

    public void addWarning(UUID uuid, String by, String reason) {
        int count = modData.getInt("warnings." + uuid + ".count", 0);
        count++;
        modData.set("warnings." + uuid + ".count", count);
        modData.set("warnings." + uuid + ".w" + count + ".by", by);
        modData.set("warnings." + uuid + ".w" + count + ".reason", reason);
        modData.set("warnings." + uuid + ".w" + count + ".date", System.currentTimeMillis());
        save();
    }

    public int getWarningCount(UUID uuid) {
        return modData.getInt("warnings." + uuid + ".count", 0);
    }

    public List<String> getWarnings(UUID uuid) {
        List<String> list = new ArrayList<>();
        int count = getWarningCount(uuid);
        for (int i = 1; i <= count; i++) {
            String path = "warnings." + uuid + ".w" + i;
            String by = modData.getString(path + ".by", "?");
            String reason = modData.getString(path + ".reason", "?");
            list.add("§7#" + i + " §cpar §f" + by + " §7- §e" + reason);
        }
        return list;
    }

    public void clearWarnings(UUID uuid) {
        modData.set("warnings." + uuid, null);
        save();
    }

    // ─── TEMPBAN ─────────────────────────────────────────────────────────────

    /**
     * Parse duration strings like "1d", "2h", "30m", "60s" → milliseconds.
     */
    public static long parseDuration(String input) {
        if (input == null) return 0;
        long total = 0;
        String num = "";
        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                num += c;
            } else {
                if (num.isEmpty()) continue;
                long n = Long.parseLong(num);
                num = "";
                total += switch (c) {
                    case 'd' -> n * 86400_000L;
                    case 'h' -> n * 3600_000L;
                    case 'm' -> n * 60_000L;
                    case 's' -> n * 1000L;
                    default -> 0;
                };
            }
        }
        return total;
    }

    public static String formatDuration(long ms) {
        if (ms <= 0) return "permanent";
        long s = ms / 1000;
        long d = s / 86400; s %= 86400;
        long h = s / 3600;  s %= 3600;
        long m = s / 60;    s %= 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("j ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
