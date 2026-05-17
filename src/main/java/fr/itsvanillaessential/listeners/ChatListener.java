package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final CoreEssentials plugin;

    public ChatListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (plugin.getModerationManager().isMuted(p.getUniqueId())) {
            event.setCancelled(true);
            String msg = plugin.getConfig().getString("chat.muted-message", "&cVous êtes muet(e).");
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(msg));
        }
    }
}
