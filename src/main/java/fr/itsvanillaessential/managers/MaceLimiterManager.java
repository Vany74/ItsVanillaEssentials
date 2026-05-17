package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MaceLimiterManager {

    private final CoreEssentials plugin;
    private File configFile;
    private FileConfiguration config;

    // UUID -> last smash attack timestamp
    private final Map<UUID, Long> smashCooldowns = new HashMap<>();

    public MaceLimiterManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "macelimiter.yml");
        if (!configFile.exists()) plugin.saveResource("macelimiter.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }

    // ─── Per-player ───────────────────────────────────────────────────────────

    public boolean isPerPlayerEnabled() { return config.getBoolean("per-player.enabled", true); }
    public int     getPerPlayerMax()    { return config.getInt("per-player.max", 1); }
    public String  getPerPlayerBypass() { return config.getString("per-player.bypass-permission", "core.macelimit.bypass"); }
    public String  getPerPlayerMsg()    { return config.getString("per-player.blocked-message", "&cLimite de maces atteinte !"); }
    public String  getPerPlayerAction() { return config.getString("per-player.action", "deny"); }

    public int countMaces(Player player) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == org.bukkit.Material.MACE) total += item.getAmount();
        }
        return total;
    }

    /** Returns true if the player exceeds the per-player mace limit */
    public boolean exceedsPerPlayerLimit(Player player) {
        if (!isEnabled() || !isPerPlayerEnabled()) return false;
        if (player.hasPermission(getPerPlayerBypass())) return false;
        return countMaces(player) > getPerPlayerMax();
    }

    /** Returns true if adding a mace to the player's inventory would exceed the limit */
    public boolean wouldExceedPerPlayerLimit(Player player) {
        if (!isEnabled() || !isPerPlayerEnabled()) return false;
        if (player.hasPermission(getPerPlayerBypass())) return false;
        return countMaces(player) >= getPerPlayerMax();
    }

    // ─── Per-team ─────────────────────────────────────────────────────────────

    public boolean isPerTeamEnabled()   { return config.getBoolean("per-team.enabled", true); }
    public int     getPerTeamMax()      { return config.getInt("per-team.max", 1); }
    public String  getPerTeamMsg()      { return config.getString("per-team.blocked-message", "&cVotre team a atteint la limite de maces !"); }
    public boolean applyPerPlayerIfNoTeam() { return config.getBoolean("per-team.apply-per-player-if-no-team", true); }

    public boolean wouldExceedTeamLimit(Player player) {
        if (!isEnabled() || !isPerTeamEnabled()) return false;
        TeamManager.Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        if (team == null) return applyPerPlayerIfNoTeam() && wouldExceedPerPlayerLimit(player);

        int teamMaces = plugin.getTeamManager().countTeamMaces(team.name);
        int maxMaces  = team.maxMaces > 0 ? team.maxMaces : getPerTeamMax();
        return teamMaces >= maxMaces;
    }

    public String getTeamLimitMessage(Player player) {
        TeamManager.Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        int max = team != null ? team.maxMaces : getPerTeamMax();
        return CoreEssentials.colorize(getPerTeamMsg().replace("{max}", String.valueOf(max)));
    }

    // ─── Smash Attack ─────────────────────────────────────────────────────────

    public boolean isSmashEnabled()     { return config.getBoolean("smash-attack.enabled", true); }
    public boolean isSmashDisabled()    { return config.getBoolean("smash-attack.disable", false); }
    public double  getMaxSmashDamage()  { return config.getDouble("smash-attack.max-damage", 0); }
    public long    getSmashCooldownSeconds() { return config.getLong("smash-attack.cooldown", 0); }
    public String  getSmashBlockedMsg() { return config.getString("smash-attack.blocked-message", "&cSmash attack en cooldown !"); }

    public List<String> getSmashDisabledWorlds() {
        return config.getStringList("smash-attack.disabled-worlds");
    }

    public boolean isSmashOnCooldown(UUID uuid) {
        long cd = getSmashCooldownSeconds();
        if (cd <= 0) return false;
        Long last = smashCooldowns.get(uuid);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < cd * 1000L;
    }

    public long getRemainingSmashCooldown(UUID uuid) {
        Long last = smashCooldowns.get(uuid);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        return getSmashCooldownSeconds() - elapsed;
    }

    public void setSmashCooldown(UUID uuid) {
        smashCooldowns.put(uuid, System.currentTimeMillis());
    }

    public List<String> getBannedWorlds() {
        return config.getStringList("banned-worlds");
    }

    // ─── Config write ─────────────────────────────────────────────────────────

    public void set(String path, Object value) {
        config.set(path, value);
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public String getRaw(String path) {
        return config.getString(path, "");
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }
}
