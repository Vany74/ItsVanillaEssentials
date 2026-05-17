package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public ReplyCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length == 0) { p.sendMessage(plugin.prefix() + "§cUsage: /reply <message>"); return true; }
        String lastTarget = plugin.getPlayerDataManager().getLastMessaged(p.getUniqueId());
        if (lastTarget == null) { p.sendMessage(plugin.prefix() + "§cAucun message récent."); return true; }
        Player target = plugin.getServer().getPlayer(lastTarget);
        if (target == null) { p.sendMessage(plugin.prefix() + "§cCe joueur n'est plus connecté."); return true; }
        String message = String.join(" ", args);
        String format = "§7[§fMP§7] §e{from} §7→ §e{to}§7: §f{msg}";
        String out = format.replace("{from}", p.getName()).replace("{to}", target.getName()).replace("{msg}", message);
        p.sendMessage(out);
        target.sendMessage(out);
        plugin.getPlayerDataManager().setLastMessaged(p.getUniqueId(), target.getName());
        plugin.getPlayerDataManager().setLastMessaged(target.getUniqueId(), p.getName());
        return true;
    }
}
