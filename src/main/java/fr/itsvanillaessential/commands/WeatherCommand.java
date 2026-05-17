package fr.itsvanillaessential.commands;

import fr.itsvanillaessential.CoreEssentials;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class WeatherCommand implements CommandExecutor {
    private final CoreEssentials plugin;
    public WeatherCommand(CoreEssentials plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("core.weather")) { sender.sendMessage(plugin.prefix() + CoreEssentials.colorize(plugin.getConfig().getString("messages.no-permission"))); return true; }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "§cUsage: /weather <sun|rain|thunder>"); return true; }
        World world = sender instanceof Player p ? p.getWorld() : plugin.getServer().getWorlds().get(0);
        switch (args[0].toLowerCase()) {
            case "sun", "clear" -> { world.setStorm(false); world.setThundering(false); sender.sendMessage(plugin.prefix() + "§aMétéo: ☀ Ensoleillé"); }
            case "rain"         -> { world.setStorm(true); world.setThundering(false); sender.sendMessage(plugin.prefix() + "§9Météo: 🌧 Pluie"); }
            case "thunder"      -> { world.setStorm(true); world.setThundering(true); sender.sendMessage(plugin.prefix() + "§8Météo: ⚡ Orage"); }
            default             -> sender.sendMessage(plugin.prefix() + "§cValeur invalide. Utilisez: sun, rain, thunder");
        }
        return true;
    }
}
