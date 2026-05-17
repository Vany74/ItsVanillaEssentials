package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;

public class CoreEssentialsCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public CoreEssentialsCommand(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.admin")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6§lCoreEssentials §fv" + plugin.getDescription().getVersion());
            sender.sendMessage("§7/ce reload §f- Recharger tous les fichiers de config");
            sender.sendMessage("§7/ce reload messages §f- Recharger messages.yml");
            sender.sendMessage("§7/ce reload crafts §f- Recharger les crafts");
            sender.sendMessage("§7/ce reload commandblocker §f- Recharger le command blocker");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            String target = args.length > 1 ? args[1].toLowerCase() : "all";

            switch (target) {
                case "messages" -> {
                    plugin.getMessages().reload();
                    sender.sendMessage(plugin.prefix() + "§amessages.yml rechargé !");
                }
                case "crafts", "craft", "craftlimit", "itemlimit", "macelimit", "teams" -> {
                    plugin.getCustomCraftManager().reload();
                    plugin.getCraftLimiterManager().reload();
                    plugin.getItemLimiterManager().reload();
                    plugin.getMaceLimiterManager().reload();
                    plugin.getTeamManager().reload();
                    sender.sendMessage(plugin.prefix() + "§aCrafts personnalisés rechargés !");
                }
                case "commandblocker", "cb" -> {
                    plugin.getCommandBlockerManager().reload();
                    sender.sendMessage(plugin.prefix() + "§aCommandBlocker rechargé !");
                }
                default -> {
                    plugin.reloadConfig();
                    plugin.getMessages().reload();
                    plugin.getCommandBlockerManager().reload();
                    plugin.getCustomCraftManager().reload();
                    plugin.getCraftLimiterManager().reload();
                    plugin.getItemLimiterManager().reload();
                    plugin.getMaceLimiterManager().reload();
                    plugin.getTeamManager().reload();
                    sender.sendMessage(plugin.prefix() + "§aTous les fichiers rechargés !");
                }
            }
        }
        return true;
    }
}
