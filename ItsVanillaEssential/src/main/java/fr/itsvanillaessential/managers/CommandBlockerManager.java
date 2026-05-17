package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandBlockerManager {

    private final CoreEssentials plugin;
    private FileConfiguration config;
    private File configFile;
    private final List<BlockedCommand> rules = new ArrayList<>();

    public CommandBlockerManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "commandblocker.yml");
        if (!configFile.exists()) plugin.saveResource("commandblocker.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
        loadRules();
    }

    private void loadRules() {
        rules.clear();
        if (!config.getBoolean("enabled", true)) return;
        List<?> list = config.getList("blocked-commands");
        if (list == null) return;
        for (Object obj : list) {
            ConfigurationSection section = null;
            if (obj instanceof ConfigurationSection cs) {
                section = cs;
            } else if (obj instanceof java.util.Map<?,?> map) {
                section = parseMap(map);
            }
            if (section == null) continue;
            try {
                BlockedCommand bc = new BlockedCommand();
                bc.command          = section.getString("command", "").toLowerCase().replace("/", "");
                bc.bypassPermission = section.getString("bypass-permission", "");
                bc.worlds           = section.getStringList("worlds");
                bc.gamemodes        = parseGameModes(section.getStringList("gamemodes"));
                ConfigurationSection actions = section.getConfigurationSection("actions");
                if (actions != null) {
                    bc.block         = actions.getBoolean("block", true);
                    bc.message       = actions.getString("message", "");
                    bc.notifyAdmins  = actions.getBoolean("notify-admins", false);
                    bc.runCommands   = actions.getStringList("run-commands");
                }
                if (!bc.command.isEmpty()) rules.add(bc);
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lecture règle CommandBlocker: " + e.getMessage());
            }
        }
        plugin.getLogger().info("[CommandBlocker] " + rules.size() + " règle(s) chargée(s).");
    }

    /** Convert a raw YAML map entry into a usable ConfigurationSection */
    @SuppressWarnings("unchecked")
    private ConfigurationSection parseMap(java.util.Map<?,?> map) {
        YamlConfiguration tmp = new YamlConfiguration();
        for (java.util.Map.Entry<?,?> e : map.entrySet()) tmp.set(e.getKey().toString(), e.getValue());
        return tmp;
    }

    private List<GameMode> parseGameModes(List<String> raw) {
        List<GameMode> result = new ArrayList<>();
        for (String s : raw) {
            try { result.add(GameMode.valueOf(s.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    /**
     * Check if a command is blocked for a player.
     * Returns the matching rule, or null if not blocked.
     */
    public BlockedCommand getBlockedRule(Player player, String fullCommand) {
        if (!config.getBoolean("enabled", true)) return null;
        String globalBypass = config.getString("global-bypass-permission", "core.commandblocker.bypass");
        if (player.hasPermission(globalBypass)) return null;

        // Extract base command (without leading / and args)
        String cmd = fullCommand.toLowerCase().replaceFirst("^/", "").split(" ")[0];

        for (BlockedCommand rule : rules) {
            if (!rule.command.equals(cmd)) continue;

            // Check bypass permission
            if (!rule.bypassPermission.isEmpty() && player.hasPermission(rule.bypassPermission)) continue;

            // Check world
            if (!rule.worlds.isEmpty() && !rule.worlds.contains(player.getWorld().getName())) continue;

            // Check gamemode
            if (!rule.gamemodes.isEmpty() && !rule.gamemodes.contains(player.getGameMode())) continue;

            return rule;
        }
        return null;
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }

    public void addRule(String command, String bypassPermission, String message, boolean notifyAdmins) {
        BlockedCommand bc = new BlockedCommand();
        bc.command         = command.toLowerCase().replace("/", "");
        bc.bypassPermission = bypassPermission;
        bc.message         = message;
        bc.notifyAdmins    = notifyAdmins;
        bc.block           = true;
        rules.add(bc);
        saveRules();
    }

    public boolean removeRule(String command) {
        String cmd = command.toLowerCase().replace("/", "");
        boolean removed = rules.removeIf(r -> r.command.equals(cmd));
        if (removed) saveRules();
        return removed;
    }

    public List<BlockedCommand> getRules() { return rules; }

    private void saveRules() {
        config.set("blocked-commands", null);
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        for (BlockedCommand bc : rules) {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("command", bc.command);
            map.put("bypass-permission", bc.bypassPermission);
            map.put("worlds", bc.worlds);
            java.util.List<String> gms = new ArrayList<>();
            bc.gamemodes.forEach(g -> gms.add(g.name()));
            map.put("gamemodes", gms);
            java.util.Map<String, Object> actions = new java.util.LinkedHashMap<>();
            actions.put("block", bc.block);
            actions.put("message", bc.message);
            actions.put("notify-admins", bc.notifyAdmins);
            actions.put("run-commands", bc.runCommands);
            map.put("actions", actions);
            list.add(map);
        }
        config.set("blocked-commands", list);
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── Inner model ──────────────────────────────────────────────────────────

    public static class BlockedCommand {
        public String command          = "";
        public String bypassPermission = "";
        public List<String> worlds     = new ArrayList<>();
        public List<GameMode> gamemodes = new ArrayList<>();
        public boolean block           = true;
        public String message          = "";
        public boolean notifyAdmins    = false;
        public List<String> runCommands = new ArrayList<>();
    }

    /** Generic config setter */
    public void set(String path, Object value) {
        config.set(path, value);
        try { config.save(configFile); } catch (java.io.IOException e) { e.printStackTrace(); }
    }

}
