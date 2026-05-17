package fr.itsvanillaessential.managers;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final CoreEssentials plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessagesManager(CoreEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Merge defaults from jar
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            FileConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messages.setDefaults(defConfig);
        }
    }

    /**
     * Get a raw message string (with & colour codes, not yet translated).
     */
    public String getRaw(String path) {
        return messages.getString(path, "&cMessage manquant: " + path);
    }

    /**
     * Get a colourized message with placeholder replacements.
     * Pass placeholders as alternating key/value pairs:
     *   get("home.teleported", "home", "maison")
     */
    public String get(String path, String... placeholders) {
        String msg = getRaw(path);
        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("Nombre impair de placeholders pour: " + path);
        }
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return CoreEssentials.colorize(msg);
    }

    /**
     * Convenience: prefix + get(path, placeholders...)
     */
    public String prefixed(String path, String... placeholders) {
        return prefix() + get(path, placeholders);
    }

    public String prefix() {
        return CoreEssentials.colorize(getRaw("prefix"));
    }

    // ─── Shortcut helpers ─────────────────────────────────────────────────────

    public String noPermission()                  { return prefixed("no-permission"); }
    public String playerOnly()                    { return prefixed("player-only"); }
    public String playerNotFound(String name)     { return prefixed("player-not-found", "player", name); }

    // States
    public String stateEnabled()  { return get("gameplay.state-enabled"); }
    public String stateDisabled() { return get("gameplay.state-disabled"); }
}
