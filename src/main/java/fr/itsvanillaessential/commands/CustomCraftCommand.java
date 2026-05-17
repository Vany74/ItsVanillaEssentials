package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import fr.itsvanillaessential.managers.CustomCraftManager;
import fr.itsvanillaessential.gui.CraftEditorGUI;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class CustomCraftCommand implements CommandExecutor {

    private final CoreEssentials plugin;

    public CustomCraftCommand(CoreEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.craft.admin")) {
            sender.sendMessage(plugin.getMessages().noPermission());
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            // /craft list
            case "list" -> {
                Set<String> ids = plugin.getCustomCraftManager().getCraftIds();
                if (ids.isEmpty()) {
                    sender.sendMessage(plugin.prefix() + "§7Aucun craft personnalisé enregistré.");
                    return true;
                }
                sender.sendMessage(plugin.prefix() + "§6Crafts personnalisés §7(" + ids.size() + ")§6:");
                ids.forEach(id -> {
                    ConfigurationSection s = plugin.getCustomCraftManager().getCraftSection(id);
                    String type   = s != null ? s.getString("type", "?") : "?";
                    String result = s != null && s.getConfigurationSection("result") != null
                            ? s.getString("result.material", "?") : "?";
                    sender.sendMessage("  §7- §e" + id + " §8[" + type + "§8] §7→ §f" + result);
                });
            }

            // /craft info <id>
            case "info" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craft info <id>"); return true; }
                String id = args[1].toLowerCase();
                ConfigurationSection s = plugin.getCustomCraftManager().getCraftSection(id);
                if (s == null) { sender.sendMessage(plugin.prefix() + "§cCraft §e" + id + " §cintrouvable."); return true; }
                sender.sendMessage("§6§l━━━ §eCraft: " + id + " §6§l━━━");
                sender.sendMessage("§7Type: §f" + s.getString("type", "?"));
                ConfigurationSection res = s.getConfigurationSection("result");
                if (res != null) {
                    sender.sendMessage("§7Résultat: §f" + res.getString("material", "?")
                            + " §7x" + res.getInt("amount", 1));
                    String name = res.getString("name", "");
                    if (!name.isEmpty()) sender.sendMessage("§7Nom: §f" + CoreEssentials.colorize(name));
                    List<String> lore = res.getStringList("lore");
                    if (!lore.isEmpty()) sender.sendMessage("§7Lore: §f" + String.join(" §8| §f", lore));
                }
                if (s.getString("type","").equals("shaped")) {
                    List<String> shape = s.getStringList("shape");
                    sender.sendMessage("§7Shape: §f" + String.join(" §8/ §f", shape));
                }
                sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /craft delete <id>
            case "delete", "del", "remove" -> {
                if (args.length < 2) { sender.sendMessage(plugin.prefix() + "§cUsage: /craft delete <id>"); return true; }
                String id = args[1].toLowerCase();
                if (plugin.getCustomCraftManager().deleteCraft(id)) {
                    sender.sendMessage(plugin.prefix() + "§aCraft §e" + id + " §asupprimé !");
                } else {
                    sender.sendMessage(plugin.prefix() + "§cCraft §e" + id + " §cintrouvable.");
                }
            }

            // /craft reload
            case "reload" -> {
                plugin.getCustomCraftManager().reload();
                sender.sendMessage(plugin.prefix() + "§aCrafts personnalisés rechargés !");
            }

            // /craft addshapeless <id> <result> <amount> <ing1> [ing2...]
            case "addshapeless" -> {
                if (args.length < 5) {
                    sender.sendMessage(plugin.prefix() + "§cUsage: /craft addshapeless <id> <résultat> <quantité> <ing1> [ing2...]");
                    return true;
                }
                String id = args[1];
                Material resultMat = Material.matchMaterial(args[2]);
                if (resultMat == null) { sender.sendMessage(plugin.prefix() + "§cMatériau résultat inconnu: §e" + args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); }
                catch (NumberFormatException e) { sender.sendMessage(plugin.prefix() + "§cQuantité invalide."); return true; }

                List<Material> ingredients = new ArrayList<>();
                for (int i = 4; i < args.length; i++) {
                    Material mat = Material.matchMaterial(args[i]);
                    if (mat == null) { sender.sendMessage(plugin.prefix() + "§cIngrédient inconnu: §e" + args[i]); return true; }
                    ingredients.add(mat);
                }
                if (ingredients.isEmpty()) { sender.sendMessage(plugin.prefix() + "§cAucun ingrédient valide."); return true; }
                String savedId = plugin.getCustomCraftManager().addShapelessCraft(id, resultMat, amount, ingredients);
                sender.sendMessage(plugin.prefix() + "§aCraft shapeless §e" + savedId + " §acréé !");
            }

            // /craft addshaped <id> <result> <amount> <row1> <row2> <row3> <A:MATERIAL> [B:MATERIAL...]
            // ex: /craft addshaped mysword DIAMOND_SWORD 1 "AAA" "ABA" "AAA" A:DIAMOND B:STICK
            case "addshaped" -> {
                if (args.length < 8) {
                    sender.sendMessage(plugin.prefix() + "§cUsage: /craft addshaped <id> <résultat> <quantité> <ligne1> <ligne2> <ligne3> <A:MAT> [B:MAT...]");
                    sender.sendMessage("§7Ex: §f/craft addshaped mysword DIAMOND_SWORD 1 AAA ABA AAA A:DIAMOND B:STICK");
                    return true;
                }
                String id = args[1];
                Material resultMat = Material.matchMaterial(args[2]);
                if (resultMat == null) { sender.sendMessage(plugin.prefix() + "§cMatériau résultat inconnu: §e" + args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); }
                catch (NumberFormatException e) { sender.sendMessage(plugin.prefix() + "§cQuantité invalide."); return true; }

                String row1 = args[4], row2 = args[5], row3 = args[6];

                Map<Character, Material> ingredients = new LinkedHashMap<>();
                for (int i = 7; i < args.length; i++) {
                    String[] parts = args[i].split(":", 2);
                    if (parts.length != 2 || parts[0].length() != 1) {
                        sender.sendMessage(plugin.prefix() + "§cFormat ingrédient invalide: §e" + args[i] + " §7(attendu: A:MATERIAL)");
                        return true;
                    }
                    char c = parts[0].charAt(0);
                    Material mat = Material.matchMaterial(parts[1]);
                    if (mat == null) { sender.sendMessage(plugin.prefix() + "§cMatériau inconnu: §e" + parts[1]); return true; }
                    ingredients.put(c, mat);
                }
                if (ingredients.isEmpty()) { sender.sendMessage(plugin.prefix() + "§cAucun ingrédient valide."); return true; }

                String savedId = plugin.getCustomCraftManager().addShapedCraft(id, resultMat, amount, row1, row2, row3, ingredients);
                sender.sendMessage(plugin.prefix() + "§aCraft shaped §e" + savedId + " §acréé !");
                sender.sendMessage("§7Shape: §f" + row1 + " / " + row2 + " / " + row3);
            }

            // /craft gui <id_nouveau> - GUI interactif pour créer un craft shaped
            case "gui" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(plugin.getMessages().playerOnly());
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.prefix() + "§cUsage: /craft gui <id>");
                    return true;
                }
                String id = args[1].toLowerCase().replaceAll("[^a-z0-9_]", "_");
                if (plugin.getCustomCraftManager().getCraftSection(id) != null) {
                    sender.sendMessage(plugin.prefix() + "§cUn craft avec l'id §e" + id + " §cexiste déjà. Choisissez un autre nom ou supprimez-le d'abord.");
                    return true;
                }
                new CraftEditorGUI(plugin, p, id).open();
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━ §eCrafts Personnalisés §6§l━━━━━");
        sender.sendMessage("§e/craft list                           §7- Lister les crafts");
        sender.sendMessage("§e/craft info §f<id>                    §7- Détails d'un craft");
        sender.sendMessage("§e/craft delete §f<id>                  §7- Supprimer un craft");
        sender.sendMessage("§e/craft reload                         §7- Recharger les crafts");
        sender.sendMessage("§e/craft gui §f<id>                     §7- Créer via GUI (recommandé)");
        sender.sendMessage("§e/craft addshaped §f<id> <résultat> <qté> <r1> <r2> <r3> <A:MAT>...");
        sender.sendMessage("§e/craft addshapeless §f<id> <résultat> <qté> <ing1> [ing2...]");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
