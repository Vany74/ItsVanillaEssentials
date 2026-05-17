package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CraftLimitCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public CraftLimitCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.craftlimit")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "list" -> {
                var mats = plugin.getCraftLimiterManager().getLimitedMaterials();
                if (mats.isEmpty()) { sender.sendMessage(plugin.prefix() + "§7Aucune limite de craft configurée."); return true; }
                sender.sendMessage(plugin.prefix() + "§6Limites de craft §7(" + mats.size() + ")§6:");
                mats.forEach(m -> {
                    Material mat = Material.matchMaterial(m);
                    if (mat == null) return;
                    int max = plugin.getCraftLimiterManager().getMax(mat);
                    long cd = plugin.getCraftLimiterManager().getCooldownSeconds(mat);
                    sender.sendMessage("  §7- §e" + m + " §8| §7Max: §e" + max
                            + (cd > 0 ? " §8| §7CD: §e" + cd + "s" : ""));
                });
            }

            case "info" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craftlimit info <item>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu: §e" + args[1]); return true; }
                if (!plugin.getCraftLimiterManager().hasLimit(mat)) {
                    sender.sendMessage(plugin.prefix() + "§cAucune limite pour §e" + mat.name()); return true;
                }
                sender.sendMessage("§6§l━━━ §eCraftLimit: " + mat.name() + " §6§l━━━");
                sender.sendMessage("§7Max par joueur: §e" + plugin.getCraftLimiterManager().getMax(mat));
                sender.sendMessage("§7Cooldown: §e" + plugin.getCraftLimiterManager().getCooldownSeconds(mat) + "s");
                sender.sendMessage("§7Permission: §f" + plugin.getCraftLimiterManager().getPermission(mat));
                sender.sendMessage("§7Bypass: §f" + plugin.getCraftLimiterManager().getBypassPermission(mat));
                sender.sendMessage("§7Message: §f" + plugin.getCraftLimiterManager().getBlockedMessage(mat));
                sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /craftlimit set <item> <max|cooldown|message|bypass|enabled> <valeur>
            case "set" -> {
                if (args.length < 4) { sender.sendMessage(plugin.prefix() + "§cUsage: /craftlimit set <item> <option> <valeur>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu: §e" + args[1]); return true; }
                plugin.getCraftLimiterManager().setLimit(mat, args[2], args[3]);
                sender.sendMessage(plugin.prefix() + "§aLimite de §e" + mat.name()
                        + " §a— §e" + args[2] + " §a→ §e" + args[3] + "§a.");
            }

            case "remove" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craftlimit remove <item>"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu: §e" + args[1]); return true; }
                plugin.getCraftLimiterManager().removeLimit(mat);
                sender.sendMessage(plugin.prefix() + "§aLimite de §e" + mat.name() + " §asupprimée.");
            }

            // /craftlimit check <joueur> [item]
            case "check" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craftlimit check <joueur> [item]"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                if (args.length >= 3) {
                    Material mat = Material.matchMaterial(args[2].toUpperCase());
                    if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu."); return true; }
                    int count = plugin.getCraftLimiterManager().getCraftCount(target.getUniqueId(), mat);
                    int max   = plugin.getCraftLimiterManager().getMax(mat);
                    sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §7— §f"
                            + mat.name() + "§7: §e" + count + "/" + max);
                } else {
                    sender.sendMessage(plugin.prefix() + "§6Crafts de §e" + target.getName() + "§6:");
                    plugin.getCraftLimiterManager().getLimitedMaterials().forEach(m -> {
                        Material mat = Material.matchMaterial(m);
                        if (mat == null) return;
                        int count = plugin.getCraftLimiterManager().getCraftCount(target.getUniqueId(), mat);
                        if (count > 0) sender.sendMessage("  §7- §e" + m + "§7: §f" + count + "§7/§f" + plugin.getCraftLimiterManager().getMax(mat));
                    });
                }
            }

            // /craftlimit reset <joueur> [item]
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craftlimit reset <joueur> [item]"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                if (args.length >= 3) {
                    Material mat = Material.matchMaterial(args[2].toUpperCase());
                    if (mat == null) { sender.sendMessage(plugin.prefix() + "§cItem inconnu."); return true; }
                    plugin.getCraftLimiterManager().resetCount(target.getUniqueId(), mat);
                    sender.sendMessage(plugin.prefix() + "§aCompteur de §e" + mat.name() + " §areinitialisé pour §e" + target.getName() + "§a.");
                } else {
                    plugin.getCraftLimiterManager().resetAllCounts(target.getUniqueId());
                    sender.sendMessage(plugin.prefix() + "§aTous les compteurs de §e" + target.getName() + " §aréinitialisés.");
                }
            }

            case "reload" -> {
                plugin.getCraftLimiterManager().reload();
                sender.sendMessage(plugin.prefix() + "§aCraft Limiter rechargé !");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§6§l━━━ §eCraft Limiter §6§l━━━");
        s.sendMessage("§e/craftlimit list");
        s.sendMessage("§e/craftlimit info §f<item>");
        s.sendMessage("§e/craftlimit set §f<item> <max|cooldown|message|bypass> <val>");
        s.sendMessage("§e/craftlimit remove §f<item>");
        s.sendMessage("§e/craftlimit check §f<joueur> [item]");
        s.sendMessage("§e/craftlimit reset §f<joueur> [item]");
        s.sendMessage("§e/craftlimit reload");
        s.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
    }
}
