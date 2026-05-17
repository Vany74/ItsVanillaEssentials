package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ItemLimitCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public ItemLimitCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.itemlimit")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "list" -> {
                var mats = plugin.getItemLimiterManager().getLimitedMaterials();
                if (mats.isEmpty()) { sender.sendMessage(plugin.prefix() + "§7Aucune limite d'item configurée."); return true; }
                sender.sendMessage(plugin.prefix() + "§6Limites d'items §7(" + mats.size() + ")§6:");
                mats.forEach(m -> {
                    Material mat = Material.matchMaterial(m);
                    if (mat == null) return;
                    int inv = plugin.getItemLimiterManager().getInventoryMax(mat);
                    int hb  = plugin.getItemLimiterManager().getHotbarMax(mat);
                    String action = plugin.getItemLimiterManager().getAction(mat);
                    sender.sendMessage("  §7- §e" + m
                            + (inv >= 0 ? " §8| §7inv: §e" + inv : "")
                            + (hb >= 0  ? " §8| §7hotbar: §e" + hb : "")
                            + " §8| §7action: §e" + action);
                });
            }

            case "info" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /itemlimit info <item>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null || !plugin.getItemLimiterManager().hasLimit(mat)) {
                    sender.sendMessage(plugin.prefix() + "§cAucune limite pour §e" + args[1]); return true;
                }
                sender.sendMessage("§6§l━━━ §eItemLimit: " + mat.name() + " §6§l━━━");
                int inv = plugin.getItemLimiterManager().getInventoryMax(mat);
                int hb  = plugin.getItemLimiterManager().getHotbarMax(mat);
                sender.sendMessage("§7Max inventaire: §e" + (inv >= 0 ? inv : "Illimité"));
                sender.sendMessage("§7Max hotbar: §e" + (hb >= 0 ? hb : "Illimité"));
                sender.sendMessage("§7Held bloqué: §e" + plugin.getItemLimiterManager().isHeldBlocked(mat));
                sender.sendMessage("§7Pickup bloqué: §e" + plugin.getItemLimiterManager().isPickupBlocked(mat));
                sender.sendMessage("§7Drop bloqué: §e" + plugin.getItemLimiterManager().isDropBlocked(mat));
                sender.sendMessage("§7Action: §e" + plugin.getItemLimiterManager().getAction(mat));
                sender.sendMessage("§7Bypass: §f" + plugin.getItemLimiterManager().getBypassPermission(mat));
                sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /itemlimit set <item> <inventory|hotbar|held|pickup|drop|use|action|bypass> <valeur>
            case "set" -> {
                if (args.length < 4) { sender.sendMessage(plugin.prefix() + "§cUsage: /itemlimit set <item> <mode> <valeur>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu: §e" + args[1]); return true; }
                plugin.getItemLimiterManager().setLimit(mat, args[2], args[3]);
                sender.sendMessage(plugin.prefix() + "§aLimite de §e" + mat.name()
                        + " §a— §e" + args[2] + " §a→ §e" + args[3] + "§a.");
            }

            case "remove" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /itemlimit remove <item>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu."); return true; }
                plugin.getItemLimiterManager().removeLimit(mat);
                sender.sendMessage(plugin.prefix() + "§aLimite de §e" + mat.name() + " §asupprimée.");
            }

            case "check" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /itemlimit check <joueur> [item]"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                sender.sendMessage(plugin.prefix() + "§6Items limités pour §e" + target.getName() + "§6:");
                plugin.getItemLimiterManager().getLimitedMaterials().forEach(m -> {
                    Material mat = Material.matchMaterial(m);
                    if (mat == null) return;
                    int inv = plugin.getItemLimiterManager().countInInventory(target, mat);
                    int max = plugin.getItemLimiterManager().getInventoryMax(mat);
                    if (inv > 0) sender.sendMessage("  §7- §e" + m + "§7: §f" + inv + (max >= 0 ? "§7/§f" + max : ""));
                });
            }

            case "reload" -> {
                plugin.getItemLimiterManager().reload();
                sender.sendMessage(plugin.prefix() + "§aItem Limiter rechargé !");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§6§l━━━ §eItem Limiter §6§l━━━");
        s.sendMessage("§e/itemlimit list");
        s.sendMessage("§e/itemlimit info §f<item>");
        s.sendMessage("§e/itemlimit set §f<item> <inventory|hotbar|held|pickup|drop|action|bypass> <val>");
        s.sendMessage("§e/itemlimit remove §f<item>");
        s.sendMessage("§e/itemlimit check §f<joueur> [item]");
        s.sendMessage("§e/itemlimit reload");
        s.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
    }
}
