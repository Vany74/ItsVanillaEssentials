package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public GiveCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.give")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /give <joueur> <item> [quantité]"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found"))); return true; }
        Material mat = Material.matchMaterial(args[1]);
        if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem §e" + args[1] + " §cinconnu."); return true; }
        int amount = 1;
        if (args.length > 2) {
            try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[2]))); }
            catch (NumberFormatException e) { sender.sendMessage(plugin.prefix() + "§cQuantité invalide."); return true; }
        }
        target.getInventory().addItem(new ItemStack(mat, amount));
        target.sendMessage(plugin.prefix() + "§aVous avez reçu §e" + amount + "x " + mat.name().toLowerCase() + "§a.");
        sender.sendMessage(plugin.prefix() + "§aDonné §e" + amount + "x " + mat.name().toLowerCase() + " §aà §e" + target.getName() + "§a.");
        return true;
    }
}
