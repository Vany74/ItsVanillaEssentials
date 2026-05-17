package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {

    private final CoreEssentials plugin;
    private File configFile;
    private FileConfiguration config;

    // teamName (lowercase) -> Team
    private final Map<String, Team> teams = new LinkedHashMap<>();
    // UUID -> teamName
    private final Map<UUID, String> playerTeams = new HashMap<>();

    public TeamManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "teams.yml");
        if (!configFile.exists()) plugin.saveResource("teams.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
        loadTeams();
    }

    private void loadTeams() {
        teams.clear();
        playerTeams.clear();

        var teamsSection = config.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String name : teamsSection.getKeys(false)) {
                String path  = "teams." + name;
                Team t       = new Team();
                t.name       = name;
                t.displayName= config.getString(path + ".display-name", name);
                t.color      = config.getString(path + ".color", "white");
                t.prefix     = config.getString(path + ".prefix", "");
                t.maxMembers = config.getInt(path + ".max-members",
                        config.getInt("default-max-members", 0));
                t.maxMaces   = config.getInt(path + ".max-maces",
                        config.getInt("default-max-maces", 1));
                t.members    = new HashSet<>(config.getStringList(path + ".members")
                        .stream().map(UUID::fromString).toList());
                teams.put(name.toLowerCase(), t);
            }
        }

        // Build reverse map
        for (Team t : teams.values()) {
            for (UUID uuid : t.members) playerTeams.put(uuid, t.name.toLowerCase());
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    public boolean createTeam(String name, String color) {
        if (teams.containsKey(name.toLowerCase())) return false;
        Team t       = new Team();
        t.name       = name;
        t.displayName= name;
        t.color      = color;
        t.prefix     = "";
        t.maxMembers = config.getInt("default-max-members", 0);
        t.maxMaces   = config.getInt("default-max-maces", 1);
        t.members    = new HashSet<>();
        teams.put(name.toLowerCase(), t);
        save();
        return true;
    }

    public boolean deleteTeam(String name) {
        Team t = teams.remove(name.toLowerCase());
        if (t == null) return false;
        t.members.forEach(playerTeams::remove);
        config.set("teams." + name.toLowerCase(), null);
        save();
        return true;
    }

    public boolean addPlayer(UUID uuid, String teamName) {
        Team t = getTeam(teamName);
        if (t == null) return false;
        // Remove from old team first
        removePlayer(uuid);
        t.members.add(uuid);
        playerTeams.put(uuid, teamName.toLowerCase());
        save();
        return true;
    }

    public boolean removePlayer(UUID uuid) {
        String old = playerTeams.remove(uuid);
        if (old == null) return false;
        Team t = teams.get(old);
        if (t != null) t.members.remove(uuid);
        save();
        return true;
    }

    // ─── Mace tracking ────────────────────────────────────────────────────────

    /** Count how many maces members of a team currently hold in their inventory */
    public int countTeamMaces(String teamName) {
        Team t = getTeam(teamName);
        if (t == null) return 0;
        int total = 0;
        for (UUID uuid : t.members) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p == null) continue;
            for (var item : p.getInventory().getContents()) {
                if (item != null && item.getType() == org.bukkit.Material.MACE) total += item.getAmount();
            }
        }
        return total;
    }

    public int getTeamMaxMaces(String teamName) {
        Team t = getTeam(teamName);
        return t != null ? t.maxMaces : config.getInt("default-max-maces", 1);
    }

    public void setTeamMaxMaces(String teamName, int max) {
        Team t = getTeam(teamName);
        if (t == null) return;
        t.maxMaces = max;
        config.set("teams." + teamName.toLowerCase() + ".max-maces", max);
        save();
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setMaxMembers(String teamName, int max) {
        Team t = getTeam(teamName);
        if (t == null) return;
        t.maxMembers = max;
        config.set("teams." + teamName.toLowerCase() + ".max-members", max);
        save();
    }

    public void setPrefix(String teamName, String prefix) {
        Team t = getTeam(teamName);
        if (t == null) return;
        t.prefix = prefix;
        config.set("teams." + teamName.toLowerCase() + ".prefix", prefix);
        save();
    }

    public void setColor(String teamName, String color) {
        Team t = getTeam(teamName);
        if (t == null) return;
        t.color = color;
        config.set("teams." + teamName.toLowerCase() + ".color", color);
        save();
    }

    public void setDisplayName(String teamName, String displayName) {
        Team t = getTeam(teamName);
        if (t == null) return;
        t.displayName = displayName;
        config.set("teams." + teamName.toLowerCase() + ".display-name", displayName);
        save();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public Team getTeam(String name)         { return name == null ? null : teams.get(name.toLowerCase()); }
    public Team getPlayerTeam(UUID uuid)     { String n = playerTeams.get(uuid); return n == null ? null : teams.get(n); }
    public String getPlayerTeamName(UUID uuid){ return playerTeams.get(uuid); }
    public Collection<Team> getAllTeams()    { return teams.values(); }
    public boolean teamExists(String name)   { return teams.containsKey(name.toLowerCase()); }

    public String formatColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red"         -> "§c";
            case "dark_red"    -> "§4";
            case "blue"        -> "§9";
            case "dark_blue"   -> "§1";
            case "green"       -> "§a";
            case "dark_green"  -> "§2";
            case "yellow"      -> "§e";
            case "gold"        -> "§6";
            case "aqua"        -> "§b";
            case "dark_aqua"   -> "§3";
            case "light_purple"-> "§d";
            case "dark_purple" -> "§5";
            case "white"       -> "§f";
            case "gray"        -> "§7";
            case "dark_gray"   -> "§8";
            case "black"       -> "§0";
            default            -> "§f";
        };
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private void save() {
        for (Team t : teams.values()) {
            String path = "teams." + t.name.toLowerCase();
            config.set(path + ".display-name", t.displayName);
            config.set(path + ".color",         t.color);
            config.set(path + ".prefix",        t.prefix);
            config.set(path + ".max-members",   t.maxMembers);
            config.set(path + ".max-maces",     t.maxMaces);
            List<String> memberList = new ArrayList<>();
            t.members.forEach(u -> memberList.add(u.toString()));
            config.set(path + ".members", memberList);
        }
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── Team model ───────────────────────────────────────────────────────────

    public static class Team {
        public String      name;
        public String      displayName;
        public String      color;
        public String      prefix;
        public int         maxMembers;
        public int         maxMaces;
        public Set<UUID>   members = new HashSet<>();
    }
}
