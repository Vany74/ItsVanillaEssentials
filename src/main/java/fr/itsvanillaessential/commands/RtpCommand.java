package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RtpCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    private final Random random = new Random();

    public RtpCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("core.rtp")) {
            p.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        p.sendMessage(plugin.prefix() + "§eRecherche d'une position aléatoire...");

        int max = plugin.getConfig().getInt("teleport.rtp-radius", 5000);
        int min = plugin.getConfig().getInt("teleport.rtp-min-radius", 100);
        World world = p.getWorld();

        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                if (attempts++ > 20) {
                    cancel();
                    p.sendMessage(plugin.prefix() + "§cImpossible de trouver une position. Réessayez.");
                    return;
                }
                int sign = random.nextBoolean() ? 1 : -1;
                int x = sign * (min + random.nextInt(max - min));
                sign = random.nextBoolean() ? 1 : -1;
                int z = sign * (min + random.nextInt(max - min));
                int y = world.getHighestBlockYAt(x, z) + 1;
                Location loc = new Location(world, x + 0.5, y, z + 0.5);

                // Safety check
                if (loc.getBlock().getType().isSolid()) return;

                cancel();
                plugin.getPlayerDataManager().setBackLocation(p.getUniqueId(), p.getLocation());
                p.teleport(loc);
                p.sendMessage(plugin.prefix() + "§aTéléporté aléatoirement en §e" + x + "§a, §e" + y + "§a, §e" + z + "§a !");
            }
        }.runTaskTimer(plugin, 0L, 5L);
        return true;
    }
}
