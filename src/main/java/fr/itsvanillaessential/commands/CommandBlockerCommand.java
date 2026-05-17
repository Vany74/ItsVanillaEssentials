package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.CommandBlockerManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandBlockerCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public CommandBlockerCommand(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.commandblocker")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /cb add <commande> [permission-bypass] [message...]
            case "add" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.prefix() + "§cUsage: /cb add <commande> [permission-bypass] [message...]");
                    return true;
                }
                String command = args[1].toLowerCase().replace("/", "");
                String bypass  = args.length > 2 ? args[2] : "";
                String message = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "";
                plugin.getCommandBlockerManager().addRule(command, bypass, message, true);
                sender.sendMessage(plugin.prefix() + "§aCommande §e/" + command + " §abloquée !");
                if (!bypass.isEmpty())
                    sender.sendMessage(plugin.prefix() + "§7Bypass: §f" + bypass);
            }

            // /cb remove <commande>
            case "remove", "delete", "del" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.prefix() + "§cUsage: /cb remove <commande>");
                    return true;
                }
                String command = args[1].toLowerCase().replace("/", "");
                if (plugin.getCommandBlockerManager().removeRule(command)) {
                    sender.sendMessage(plugin.prefix() + "§aCommande §e/" + command + " §adébloquée !");
                } else {
                    sender.sendMessage(plugin.prefix() + "§cAucune règle trouvée pour §e/" + command + "§c.");
                }
            }

            // /cb list
            case "list" -> {
                List<CommandBlockerManager.BlockedCommand> rules = plugin.getCommandBlockerManager().getRules();
                if (rules.isEmpty()) {
                    sender.sendMessage(plugin.prefix() + "§7Aucune commande bloquée.");
                    return true;
                }
                sender.sendMessage(plugin.prefix() + "§6Commandes bloquées §7(" + rules.size() + ")§6:");
                for (CommandBlockerManager.BlockedCommand r : rules) {
                    String worlds  = r.worlds.isEmpty()    ? "§atous" : "§e" + String.join("§7,§e", r.worlds);
                    String gms     = r.gamemodes.isEmpty() ? "§atous" : "§e" + r.gamemodes.size() + " gm";
                    String bypass  = r.bypassPermission.isEmpty() ? "§7-" : "§f" + r.bypassPermission;
                    sender.sendMessage("  §c/" + r.command + " §8| §7bypass: " + bypass
                            + " §8| §7monde: " + worlds + " §8| §7gm: " + gms
                            + " §8| §7notify: " + (r.notifyAdmins ? "§aoui" : "§cnon"));
                }
            }

            // /cb info <commande>
            case "info" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /cb info <commande>"); return true; }
                String command = args[1].toLowerCase().replace("/", "");
                plugin.getCommandBlockerManager().getRules().stream()
                        .filter(r -> r.command.equals(command))
                        .findFirst()
                        .ifPresentOrElse(r -> {
                            sender.sendMessage("§6§l━━━ §eCommandBlocker: /" + r.command + " §6§l━━━");
                            sender.sendMessage("§7Bypass permission: §f" + (r.bypassPermission.isEmpty() ? "§7-" : r.bypassPermission));
                            sender.sendMessage("§7Message custom: §f" + (r.message.isEmpty() ? "§7(défaut)" : r.message));
                            sender.sendMessage("§7Notif admins: §f" + (r.notifyAdmins ? "§aOui" : "§cNon"));
                            sender.sendMessage("§7Mondes: §f" + (r.worlds.isEmpty() ? "§aTous" : String.join(", ", r.worlds)));
                            sender.sendMessage("§7Gamemodes: §f" + (r.gamemodes.isEmpty() ? "§aTous" : r.gamemodes.size() + " configuré(s)"));
                            if (!r.runCommands.isEmpty())
                                sender.sendMessage("§7Cmds à exécuter: §f" + String.join("§7, §f", r.runCommands));
                            sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        }, () -> sender.sendMessage(plugin.prefix() + "§cAucune règle pour §e/" + command + "§c."));
            }

            // /cb reload
            case "reload" -> {
                plugin.getCommandBlockerManager().reload();
                sender.sendMessage(plugin.prefix() + "§aCommandBlocker rechargé !");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━ §eCommandBlocker §6§l━━━");
        sender.sendMessage("§e/cb add §f<cmd> [bypass-perm] [message]  §7- Bloquer une commande");
        sender.sendMessage("§e/cb remove §f<cmd>                        §7- Débloquer une commande");
        sender.sendMessage("§e/cb list                                   §7- Lister les règles");
        sender.sendMessage("§e/cb info §f<cmd>                           §7- Détails d'une règle");
        sender.sendMessage("§e/cb reload                                 §7- Recharger le fichier");
    }
}
