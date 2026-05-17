package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeamCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public TeamCommand(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.team")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            // /team create <nom> [couleur]
            case "create" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /team create <nom> [couleur]"); return true; }
                String name  = args[1];
                String color = args.length > 2 ? args[2].toLowerCase() : "white";
                if (plugin.getTeamManager().createTeam(name, color)) {
                    sender.sendMessage(plugin.prefix() + "§aTeam §e" + name + " §acréée avec la couleur §e" + color + "§a !");
                } else {
                    sender.sendMessage(plugin.prefix() + "§cUne team §e" + name + " §cexiste déjà.");
                }
            }

            // /team delete <nom>
            case "delete", "del" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /team delete <nom>"); return true; }
                if (plugin.getTeamManager().deleteTeam(args[1])) {
                    sender.sendMessage(plugin.prefix() + "§aTeam §e" + args[1] + " §asupprimée.");
                } else {
                    sender.sendMessage(plugin.prefix() + "§cTeam §e" + args[1] + " §cintrouvable.");
                }
            }

            // /team add <joueur> <team>
            case "add" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team add <joueur> <team>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                TeamManager.Team t = plugin.getTeamManager().getTeam(args[2]);
                if (t == null) { sender.sendMessage(plugin.prefix() + "§cTeam §e" + args[2] + " §cintrouvable."); return true; }
                // Max members check
                if (t.maxMembers > 0 && t.members.size() >= t.maxMembers) {
                    sender.sendMessage(plugin.prefix() + "§cLa team §e" + t.name + " §cest pleine §7(" + t.maxMembers + " max)§c.");
                    return true;
                }
                plugin.getTeamManager().addPlayer(target.getUniqueId(), args[2]);
                String color = plugin.getTeamManager().formatColor(t.color);
                sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §aajouté à la team " + color + t.displayName + "§a.");
                target.sendMessage(plugin.prefix() + "§aVous avez rejoint la team " + color + t.displayName + "§a !");
            }

            // /team remove <joueur>
            case "remove" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /team remove <joueur>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.getMessages().playerNotFound(args[1])); return true; }
                if (plugin.getTeamManager().removePlayer(target.getUniqueId())) {
                    sender.sendMessage(plugin.prefix() + "§e" + target.getName() + " §aretired de sa team.");
                    target.sendMessage(plugin.prefix() + "§aVous avez quitté votre team.");
                } else {
                    sender.sendMessage(plugin.prefix() + "§e" + args[1] + " §cn'est dans aucune team.");
                }
            }

            // /team list
            case "list" -> {
                var teams = plugin.getTeamManager().getAllTeams();
                if (teams.isEmpty()) { sender.sendMessage(plugin.prefix() + "§7Aucune team créée."); return true; }
                sender.sendMessage(plugin.prefix() + "§6Teams §7(" + teams.size() + ")§6:");
                for (var t : teams) {
                    String color = plugin.getTeamManager().formatColor(t.color);
                    int maces = plugin.getTeamManager().countTeamMaces(t.name);
                    sender.sendMessage("  " + color + t.displayName + " §8| §7Membres: §e"
                            + t.members.size() + (t.maxMembers > 0 ? "/" + t.maxMembers : "")
                            + " §8| §7Maces: §e" + maces + "/" + t.maxMaces);
                }
            }

            // /team info <nom>
            case "info" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /team info <nom>"); return true; }
                TeamManager.Team t = plugin.getTeamManager().getTeam(args[1]);
                if (t == null) { sender.sendMessage(plugin.prefix() + "§cTeam §e" + args[1] + " §cintrouvable."); return true; }
                String color = plugin.getTeamManager().formatColor(t.color);
                sender.sendMessage("§6§l━━━ §eTeam: " + color + t.displayName + " §6§l━━━");
                sender.sendMessage("§7Nom interne: §f" + t.name);
                sender.sendMessage("§7Couleur: §f" + t.color);
                sender.sendMessage("§7Préfixe: §f" + (t.prefix.isEmpty() ? "§7Aucun" : t.prefix));
                sender.sendMessage("§7Membres §7(" + t.members.size() + (t.maxMembers > 0 ? "/" + t.maxMembers : "") + ")§7:");
                t.members.forEach(u -> {
                    Player p = Bukkit.getPlayer(u);
                    String pName = p != null ? "§a" + p.getName() + " §7(en ligne)" : "§7" + u.toString().substring(0, 8) + "...";
                    sender.sendMessage("  §8- " + pName);
                });
                int maces = plugin.getTeamManager().countTeamMaces(t.name);
                sender.sendMessage("§7Maces en jeu: §e" + maces + "/" + t.maxMaces);
                sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /team chat <message>
            case "chat", "c" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getMessages().playerOnly()); return true; }
                TeamManager.Team t = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());
                if (t == null) { p.sendMessage(plugin.prefix() + "§cVous n'êtes dans aucune team."); return true; }
                if (args.length < 2) { p.sendMessage(plugin.prefix() + "§cUsage: /team chat <message>"); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String color = plugin.getTeamManager().formatColor(t.color);
                String format = plugin.getConfig().getString(
                        "team-chat-format", "&8[&{color}{team}&8] &7{player}&8: &f{message}");
                String out = CoreEssentials.colorize(format
                        .replace("{color}", t.color)
                        .replace("{team}", t.displayName)
                        .replace("{player}", p.getName())
                        .replace("{message}", msg));
                t.members.forEach(u -> {
                    Player member = Bukkit.getPlayer(u);
                    if (member != null) member.sendMessage(out);
                });
                // Also send to ops/admins with core.team.spy
                Bukkit.getOnlinePlayers().stream()
                        .filter(pl -> pl.hasPermission("core.team.spy") && !t.members.contains(pl.getUniqueId()))
                        .forEach(pl -> pl.sendMessage("§8[Spy] " + out));
            }

            // /team setmax <team> <nb>
            case "setmax" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team setmax <team> <nb>"); return true; }
                if (!plugin.getTeamManager().teamExists(args[1])) { sender.sendMessage(plugin.prefix() + "§cTeam introuvable."); return true; }
                try {
                    int max = Integer.parseInt(args[2]);
                    plugin.getTeamManager().setMaxMembers(args[1], max);
                    sender.sendMessage(plugin.prefix() + "§aMax membres de §e" + args[1] + " §a→ §e" + max + "§a.");
                } catch (NumberFormatException e) { sender.sendMessage(plugin.prefix() + "§cNombre invalide."); }
            }

            // /team setmaces <team> <nb>
            case "setmaces" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team setmaces <team> <nb>"); return true; }
                if (!plugin.getTeamManager().teamExists(args[1])) { sender.sendMessage(plugin.prefix() + "§cTeam introuvable."); return true; }
                try {
                    int max = Integer.parseInt(args[2]);
                    plugin.getTeamManager().setTeamMaxMaces(args[1], max);
                    sender.sendMessage(plugin.prefix() + "§aMaces max de §e" + args[1] + " §a→ §e" + max + "§a.");
                } catch (NumberFormatException e) { sender.sendMessage(plugin.prefix() + "§cNombre invalide."); }
            }

            // /team prefix <team> <préfixe>
            case "prefix" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team prefix <team> <préfixe>"); return true; }
                if (!plugin.getTeamManager().teamExists(args[1])) { sender.sendMessage(plugin.prefix() + "§cTeam introuvable."); return true; }
                String prefix = CoreEssentials.colorize(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                plugin.getTeamManager().setPrefix(args[1], prefix);
                sender.sendMessage(plugin.prefix() + "§aPréfixe de §e" + args[1] + " §a→ " + prefix);
            }

            // /team color <team> <couleur>
            case "color", "colour" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team color <team> <couleur>"); return true; }
                plugin.getTeamManager().setColor(args[1], args[2].toLowerCase());
                sender.sendMessage(plugin.prefix() + "§aCouleur de §e" + args[1] + " §a→ §e" + args[2] + "§a.");
            }

            // /team rename <team> <nouveau nom affiché>
            case "rename" -> {
                if (args.length < 3) { sender.sendMessage(plugin.prefix() + "§cUsage: /team rename <team> <affichage>"); return true; }
                String display = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                plugin.getTeamManager().setDisplayName(args[1], display);
                sender.sendMessage(plugin.prefix() + "§aNom affiché de §e" + args[1] + " §a→ §f" + display + "§a.");
            }

            // /team reload
            case "reload" -> {
                plugin.getTeamManager().reload();
                sender.sendMessage(plugin.prefix() + "§aTeams rechargées !");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━ §eTeams §6§l━━━━━");
        sender.sendMessage("§e/team create §f<nom> [couleur]      §7Créer une team");
        sender.sendMessage("§e/team delete §f<nom>                §7Supprimer une team");
        sender.sendMessage("§e/team add §f<joueur> <team>         §7Ajouter un joueur");
        sender.sendMessage("§e/team remove §f<joueur>             §7Retirer un joueur");
        sender.sendMessage("§e/team list                          §7Lister les teams");
        sender.sendMessage("§e/team info §f<nom>                  §7Détails d'une team");
        sender.sendMessage("§e/team chat §f<message>              §7Chat de team");
        sender.sendMessage("§e/team setmax §f<team> <nb>          §7Limite de membres");
        sender.sendMessage("§e/team setmaces §f<team> <nb>        §7Limite de maces");
        sender.sendMessage("§e/team prefix §f<team> <préfixe>     §7Définir le préfixe");
        sender.sendMessage("§e/team color §f<team> <couleur>      §7Changer la couleur");
        sender.sendMessage("§e/team rename §f<team> <affichage>   §7Nom d'affichage");
        sender.sendMessage("§e/team reload                        §7Recharger");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
    }
}
