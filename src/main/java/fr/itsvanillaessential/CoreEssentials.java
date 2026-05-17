package fr.itsvanillaessential;

import fr.itsvanillaessential.commands.*;
import fr.itsvanillaessential.listeners.*;
import fr.itsvanillaessential.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CoreEssentials extends JavaPlugin {

    private static CoreEssentials instance;

    private HomeManager           homeManager;
    private WarpManager           warpManager;
    private KitManager            kitManager;
    private TeleportManager       teleportManager;
    private PlayerDataManager     playerDataManager;
    private ModerationManager     moderationManager;
    private MessagesManager       messagesManager;
    private CommandBlockerManager commandBlockerManager;
    private CustomCraftManager    customCraftManager;
    private CraftLimiterManager   craftLimiterManager;
    private ItemLimiterManager    itemLimiterManager;
    private MaceLimiterManager    maceLimiterManager;
    private TeamManager           teamManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messagesManager       = new MessagesManager(this);
        this.playerDataManager     = new PlayerDataManager(this);
        this.homeManager           = new HomeManager(this);
        this.warpManager           = new WarpManager(this);
        this.kitManager            = new KitManager(this);
        this.teleportManager       = new TeleportManager(this);
        this.moderationManager     = new ModerationManager(this);
        this.commandBlockerManager = new CommandBlockerManager(this);
        this.customCraftManager    = new CustomCraftManager(this);
        this.craftLimiterManager   = new CraftLimiterManager(this);
        this.itemLimiterManager    = new ItemLimiterManager(this);
        this.maceLimiterManager    = new MaceLimiterManager(this);
        this.teamManager           = new TeamManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this),         this);
        getServer().getPluginManager().registerEvents(new ChatListener(this),           this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this);
        getServer().getPluginManager().registerEvents(new LimiterListener(this),        this);
        getServer().getPluginManager().registerEvents(new MaceAnnounceListener(this),   this);
        getServer().getPluginManager().registerEvents(new WorldAccessListener(this),    this);

        registerCommands();

        getLogger().info("§a╔══════════════════════════════════╗");
        getLogger().info("§a║   CoreEssentials chargé !        §a║");
        getLogger().info("§a║   CommandBlocker + CustomCraft   §a║");
        getLogger().info("§a╚══════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (playerDataManager  != null) playerDataManager.saveAll();
        if (homeManager        != null) homeManager.saveAll();
        if (customCraftManager != null) customCraftManager.unregisterAll();
        if (craftLimiterManager != null) { /* data auto-saved */ }
        if (teamManager         != null) teamManager.reload();
        getLogger().info("§cCoreEssentials désactivé.");
    }

    private void registerCommands() {
        // Téléportation
        reg("spawn",     new SpawnCommand(this));
        reg("setspawn",  new SetSpawnCommand(this));
        reg("home",      new HomeCommand(this));
        reg("sethome",   new SetHomeCommand(this));
        reg("delhome",   new DelHomeCommand(this));
        reg("homes",     new HomesCommand(this));
        reg("warp",      new WarpCommand(this));
        reg("setwarp",   new SetWarpCommand(this));
        reg("delwarp",   new DelWarpCommand(this));
        reg("tp",        new TpCommand(this));
        reg("tpa",       new TpaCommand(this));
        reg("tpaccept",  new TpAcceptCommand(this));
        reg("tpdeny",    new TpDenyCommand(this));
        reg("tphere",    new TpHereCommand(this));
        reg("back",      new BackCommand(this));
        reg("rtp",       new RtpCommand(this));
        // Messages
        reg("msg",       new MsgCommand(this));
        reg("reply",     new ReplyCommand(this));
        reg("socialspy", new SocialSpyCommand(this));
        // Gameplay
        reg("kit",       new KitCommand(this));
        reg("heal",      new HealCommand(this));
        reg("feed",      new FeedCommand(this));
        reg("fly",       new FlyCommand(this));
        reg("speed",     new SpeedCommand(this));
        reg("god",       new GodCommand(this));
        reg("invsee",    new InvSeeCommand(this));
        reg("enderchest",new EnderChestCommand(this));
        reg("workbench", new WorkbenchCommand(this));
        reg("hat",       new HatCommand(this));
        reg("repair",    new RepairCommand(this));
        reg("suicide",   new SuicideCommand(this));
        // Modération
        reg("kick",          new KickCommand(this));
        reg("ban",           new BanCommand(this));
        reg("unban",         new UnbanCommand(this));
        reg("mute",          new MuteCommand(this));
        reg("unmute",        new UnmuteCommand(this));
        reg("tempban",       new TempBanCommand(this));
        reg("warn",          new WarnCommand(this));
        reg("warnings",      new WarningsCommand(this));
        reg("clearwarnings", new ClearWarningsCommand(this));
        // Admin
        GamemodeCommand gmc = new GamemodeCommand(this);
        reg("gamemode", gmc); reg("gms", gmc); reg("gmc", gmc); reg("gma", gmc); reg("gmsp", gmc);
        reg("give",      new GiveCommand(this));
        reg("clear",     new ClearCommand(this));
        reg("time",      new TimeCommand(this));
        reg("weather",   new WeatherCommand(this));
        reg("broadcast", new BroadcastCommand(this));
        reg("nick",      new NickCommand(this));
        reg("afk",       new AfkCommand(this));
        reg("seen",      new SeenCommand(this));
        reg("whois",     new WhoisCommand(this));
        // CommandBlocker
        CommandBlockerCommand cbc = new CommandBlockerCommand(this);
        reg("commandblocker", cbc);
        reg("cb",             cbc);
        // CustomCraft
        reg("craft",        new CustomCraftCommand(this));
        reg("team",         new TeamCommand(this));
        reg("craftlimit",   new CraftLimitCommand(this));
        reg("itemlimit",    new ItemLimitCommand(this));
        reg("macelimit",    new MaceLimitCommand(this));
        reg("adminmenu",    new AdminMenuCommand(this));
        // Plugin management
        CoreEssentialsCommand cec = new CoreEssentialsCommand(this);
        reg("itsvanillaessential", cec);
        reg("ce",             cec);
    }

    private void reg(String name, org.bukkit.command.CommandExecutor executor) {
        var cmd = getCommand(name);
        if (cmd != null) cmd.setExecutor(executor);
        else getLogger().warning("Commande inconnue dans plugin.yml: /" + name);
    }

    public static CoreEssentials getInstance()              { return instance; }
    public HomeManager getHomeManager()                     { return homeManager; }
    public WarpManager getWarpManager()                     { return warpManager; }
    public KitManager getKitManager()                       { return kitManager; }
    public TeleportManager getTeleportManager()             { return teleportManager; }
    public PlayerDataManager getPlayerDataManager()         { return playerDataManager; }
    public ModerationManager getModerationManager()         { return moderationManager; }
    public MessagesManager getMessages()                    { return messagesManager; }
    public CommandBlockerManager getCommandBlockerManager() { return commandBlockerManager; }
    public CustomCraftManager getCustomCraftManager()       { return customCraftManager; }
    public CraftLimiterManager getCraftLimiterManager()     { return craftLimiterManager; }
    public ItemLimiterManager getItemLimiterManager()       { return itemLimiterManager; }
    public MaceLimiterManager getMaceLimiterManager()       { return maceLimiterManager; }
    public TeamManager getTeamManager()                     { return teamManager; }

    public String prefix() {
        return messagesManager != null ? messagesManager.prefix()
                : colorize("&8[&6Core&8] &r");
    }

    public static String colorize(String s) {
        if (s == null) return "";
        return s.replace("&", "§");
    }
}
