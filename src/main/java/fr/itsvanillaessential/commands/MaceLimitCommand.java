package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class MaceLimitCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public MaceLimitCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.macelimit")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }
        if (args.length == 0) { sendInfo(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "info" -> sendInfo(sender);

            // /macelimit set <path> <value>
            // ex: /macelimit set per-player.max 2
            //     /macelimit set smash-attack.disable true
            //     /macelimit set smash-attack.cooldown 10
            case "set" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /macelimit set <chemin> <valeur>"); return true; }
                String path  = args[1];
                String value = args[2];
                try {
                    // Try int first, then double, then boolean, then string
                    try { plugin.getMaceLimiterManager().set(path, Integer.parseInt(value)); }
                    catch (NumberFormatException e1) {
                        try { plugin.getMaceLimiterManager().set(path, Double.parseDouble(value)); }
                        catch (NumberFormatException e2) {
                            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
                                plugin.getMaceLimiterManager().set(path, Boolean.parseBoolean(value));
                            else
                                plugin.getMaceLimiterManager().set(path, value);
                        }
                    }
                    sender.sendMessage(plugin.prefix() + "§aMace config §e" + path + " §a→ §e" + value + "§a.");
                } catch (Exception e) {
                    sender.sendMessage(plugin.prefix() + "§cErreur: §e" + e.getMessage());
                }
            }

            // /macelimit check <joueur>
            case "check" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /macelimit check <joueur>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                int count  = plugin.getMaceLimiterManager().countMaces(target);
                int max    = plugin.getMaceLimiterManager().getPerPlayerMax();
                var team   = plugin.getTeamManager().getPlayerTeam(target.getUniqueId());
                sender.sendMessage(plugin.prefix() + "§6Maces de §e" + target.getName() + "§6:");
                sender.sendMessage("  §7Inventaire: §e" + count + "/" + max);
                if (team != null) {
                    int teamMaces = plugin.getTeamManager().countTeamMaces(team.name);
                    sender.sendMessage("  §7Team §e" + team.name + "§7: §e" + teamMaces + "/" + team.maxMaces);
                } else {
                    sender.sendMessage("  §7Team: §7Aucune");
                }
            }

            // /macelimit reset <joueur>  — removes extra maces
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /macelimit reset <joueur>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                // Remove all maces from inventory
                var contents = target.getInventory().getContents();
                int removed = 0;
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].getType() == org.bukkit.Material.MACE) {
                        target.getInventory().setItem(i, null);
                        removed++;
                    }
                }
                sender.sendMessage(plugin.prefix() + "§a" + removed + " mace(s) retirée(s) de §e" + target.getName() + "§a.");
                if (removed > 0)
                    target.sendMessage(plugin.prefix() + "§cVos maces ont été retirées par un administrateur.");
            }

            case "reload" -> {
                plugin.getMaceLimiterManager().reload();
                sender.sendMessage(plugin.prefix() + "§aMace Limiter rechargé !");
            }

            default -> sendInfo(sender);
        }
        return true;
    }

    private void sendInfo(CommandSender s) {
        s.sendMessage("§6§l━━━━━ §eMace Limiter §6§l━━━━━");
        s.sendMessage("§7Activé: §e" + plugin.getMaceLimiterManager().isEnabled());
        s.sendMessage("§7Limite joueur: §e" + plugin.getMaceLimiterManager().getPerPlayerMax());
        s.sendMessage("§7Limite team: §e" + plugin.getMaceLimiterManager().getPerTeamMax());
        s.sendMessage("§7Smash désactivé: §e" + plugin.getMaceLimiterManager().isSmashDisabled());
        s.sendMessage("§7Dégâts max smash: §e" + (plugin.getMaceLimiterManager().getMaxSmashDamage() <= 0 ? "Illimité" : plugin.getMaceLimiterManager().getMaxSmashDamage()));
        s.sendMessage("§7Cooldown smash: §e" + plugin.getMaceLimiterManager().getSmashCooldownSeconds() + "s");
        s.sendMessage("§7Annonce craft: §e" + plugin.getConfig().getBoolean("mace.announce-craft", true));
        s.sendMessage("§7Annonce destruction: §e" + plugin.getConfig().getBoolean("mace.announce-destroy", true));
        s.sendMessage("");
        s.sendMessage("§e/macelimit set §f<chemin> <valeur>   §7Modifier une option");
        s.sendMessage("§e/macelimit check §f<joueur>          §7Vérifier un joueur");
        s.sendMessage("§e/macelimit reset §f<joueur>          §7Retirer les maces");
        s.sendMessage("§e/macelimit reload                    §7Recharger");
        s.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
