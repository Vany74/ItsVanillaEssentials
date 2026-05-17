package fr.itsvanillaessential.listeners;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.CommandBlockerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandBlockerListener implements Listener {

    private final CoreEssentials plugin;

    public CommandBlockerListener(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player  = event.getPlayer();
        String message = event.getMessage(); // includes leading /

        CommandBlockerManager.BlockedCommand rule =
                plugin.getCommandBlockerManager().getBlockedRule(player, message);

        if (rule == null) return;

        // Block the command
        event.setCancelled(true);

        // Send message to player
        String msg;
        if (!rule.message.isEmpty()) {
            msg = plugin.prefix() + CoreEssentials.colorize(rule.message);
        } else {
            msg = plugin.getMessages().prefixed("command-blocker.blocked-message",
                    "command", rule.command);
        }
        player.sendMessage(msg);

        // Notify admins
        boolean notify = rule.notifyAdmins || plugin.getCommandBlockerManager()
                .getRules().stream().anyMatch(r -> r.command.equals(rule.command) && r.notifyAdmins);

        if (notify || plugin.getConfig().getBoolean("command-blocker.notify-admins-default", true)) {
            String notifMsg = plugin.getMessages().prefixed("command-blocker.notify-message",
                    "player", player.getName(),
                    "command", rule.command);
            plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("core.commandblocker.notify"))
                    .forEach(p -> p.sendMessage(notifMsg));
        }

        // Execute run-commands
        if (!rule.runCommands.isEmpty()) {
            for (String cmd : rule.runCommands) {
                String resolved = cmd.replace("{player}", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), resolved);
            }
        }
    }
}
