package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.ModerationManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public KitCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.kit")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        var kits = plugin.getKitManager().getKitNames();
        if (args.length == 0) {
            p.sendMessage(plugin.prefix() + "§6Kits disponibles: §e" + String.join("§7, §e", kits));
            p.sendMessage(plugin.prefix() + "§7Usage: §f/kit <nom>");
            return true;
        }
        String kitName = args[0].toLowerCase();
        if (!kits.contains(kitName)) {
            p.sendMessage(plugin.prefix() + "§cKit §e" + kitName + " §cintrouvable.");
            return true;
        }
        if (!p.hasPermission("core.kit." + kitName) && !p.hasPermission("core.kit.*")) {
            p.sendMessage(plugin.prefix() + "§cVous n'avez pas accès à ce kit.");
            return true;
        }
        if (plugin.getKitManager().isOnCooldown(p, kitName)) {
            long remaining = plugin.getKitManager().getRemainingCooldown(p, kitName);
            long cd = plugin.getKitManager().getCooldown(kitName);
            if (cd == -1) {
                p.sendMessage(plugin.prefix() + "§cVous avez déjà réclamé ce kit (usage unique).");
            } else {
                p.sendMessage(plugin.prefix() + "§cCooldown: §e" + ModerationManager.formatDuration(remaining * 1000L) + " §crestant.");
            }
            return true;
        }
        plugin.getKitManager().giveKit(p, kitName);
        p.sendMessage(plugin.prefix() + "§aKit §e" + kitName + " §arécupéré !");
        return true;
    }
}
