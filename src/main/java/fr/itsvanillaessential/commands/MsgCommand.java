package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class MsgCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public MsgCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.msg")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length < 2) { p.sendMessage(plugin.prefix() + "§cUsage: /msg <joueur> <message>"); return true; }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        String format = "§7[§fMP§7] §e{from} §7→ §e{to}§7: §f{msg}";
        String out = format.replace("{from}", p.getName()).replace("{to}", target.getName()).replace("{msg}", message);

        p.sendMessage(out);
        target.sendMessage(out);

        plugin.getPlayerDataManager().setLastMessaged(p.getUniqueId(), target.getName());
        plugin.getPlayerDataManager().setLastMessaged(target.getUniqueId(), p.getName());

        // Social spy
        plugin.getServer().getOnlinePlayers().stream()
                .filter(pl -> plugin.getPlayerDataManager().hasSocialSpy(pl.getUniqueId()))
                .filter(pl -> pl != p && pl != target)
                .forEach(pl -> pl.sendMessage("§8[SocialSpy] " + out));
        return true;
    }
}
