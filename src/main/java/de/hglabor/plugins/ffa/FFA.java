package de.hglabor.plugins.ffa;

import de.hglabor.plugins.ffa.commands.ReloadMapCommand;
import de.hglabor.plugins.ffa.commands.SuicideCommand;
import de.hglabor.plugins.ffa.config.FFAConfig;
import de.hglabor.plugins.ffa.kit.KitSelectorImpl;
import de.hglabor.plugins.ffa.listener.FFADeathListener;
import de.hglabor.plugins.ffa.listener.FFAJoinListener;
import de.hglabor.plugins.ffa.listener.FFAMoveListener;
import de.hglabor.plugins.ffa.listener.FFAQuitListener;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.LocationUtils;
import de.hglabor.plugins.ffa.util.ScoreboardManager;
import de.hglabor.plugins.ffa.world.ArenaManager;
import de.hglabor.plugins.ffa.world.ArenaSettings;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.command.KitSettingsCommand;
import de.hglabor.plugins.kitapi.kit.events.KitEventHandlerImpl;
import de.hglabor.plugins.kitapi.kit.items.KitItemHandler;
import de.hglabor.plugins.kitapi.listener.InventoryDetection;
import de.hglabor.plugins.kitapi.listener.LastHitDetection;
import de.hglabor.plugins.kitapi.pvp.CPSChecker;
import de.hglabor.plugins.kitapi.pvp.SoupHealing;
import de.hglabor.plugins.kitapi.pvp.Tracker;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.feast.FeastListener;
import de.hglabor.utils.noriskutils.listener.DamageNerf;
import de.hglabor.utils.noriskutils.listener.DurabilityFix;
import de.hglabor.utils.noriskutils.listener.OldKnockback;
import de.hglabor.utils.noriskutils.listener.RemoveHitCooldown;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Paths;

public final class FFA extends JavaPlugin {
    private final static String SHARED_SERVER_DATA = "/home/mcserver/shared_server_data/";
    private static FFA plugin;
    private static ArenaManager arenaManager;
    private static FFARunnable ffaRunnable;

    public static FFARunnable getFFARunnable() {
        return ffaRunnable;
    }

    public static ArenaManager getArenaManager() {
        return arenaManager;
    }

    public static FFA getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        KitApi.getInstance().register(PlayerList.getInstance(), new KitSelectorImpl(), this,Paths.get(SHARED_SERVER_DATA,"configs","ffa"));
        World world = Bukkit.getWorld("world");
        arenaManager = new ArenaManager(world, FFAConfig.getInteger("ffa.size"));
        ffaRunnable = new FFARunnable(world, FFAConfig.getInteger("ffa.duration"));
        ffaRunnable.runTaskTimer(this, 0, 20);
        ScoreboardManager scoreboardManager = new ScoreboardManager();
        scoreboardManager.runTaskTimer(this, 0, 20);

        CommandAPI.onEnable(this);
        this.registerListeners();
        this.registerCommands();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            FFAPlayer player = PlayerList.getInstance().getPlayer(onlinePlayer);
            onlinePlayer.setMetadata("oldKnockback", new FixedMetadataValue(FFA.getPlugin(), ""));
            PlayerList.getInstance().add(player);
            ScoreboardFactory.create(player);
            Bukkit.getOnlinePlayers().forEach(newPlayer -> {
                ScoreboardFactory.addPlayerToNoCollision(newPlayer, player);
            });
            arenaManager.prepareKitSelection(onlinePlayer);
        }
        //TODO refactorn
        new BukkitRunnable() {
            @Override
            public void run() {
                for (FFAPlayer ffaPlayer : PlayerList.getInstance().getPlayers()) {
                    ffaPlayer.getBukkitPlayer().ifPresent(p -> {
                        World pWorld = p.getWorld();
                        if (!pWorld.getWorldBorder().isInside(p.getLocation())) {
                            p.teleport(LocationUtils.getHighestBlock(pWorld, (int) (pWorld.getWorldBorder().getSize() / 2), 0).clone().add(0, 1, 0));
                        }
                    });
                }
            }
        }.runTaskTimer(this, 0, 20 * 5L);
    }

    @Override
    public void onLoad() {
        plugin = this;
        //disable this method or download localization files via https://github.com/HGLabor/LocalizationKeys/
        loadLocalizationFiles();
        FFAConfig.load();
        CommandAPI.onLoad(true);
    }

    @Override
    public void onDisable() {
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ArenaSettings(), this);
        pluginManager.registerEvents(new KitEventHandlerImpl(), this);
        pluginManager.registerEvents(new KitItemHandler(), this);
        pluginManager.registerEvents(new FFAJoinListener(), this);
        pluginManager.registerEvents(new FFAQuitListener(), this);
        pluginManager.registerEvents(new FFADeathListener(), this);
        pluginManager.registerEvents(new FFAMoveListener(), this);
        //mechanics
        pluginManager.registerEvents(new SoupHealing(), this);
        pluginManager.registerEvents(new Tracker(30D, PlayerList.getInstance()), this);
        pluginManager.registerEvents(new DamageNerf(FFAConfig.getDouble("damage.sword.nerf"), FFAConfig.getDouble("damage.other.nerf")), this);
        pluginManager.registerEvents(new DurabilityFix(), this);
        pluginManager.registerEvents(new FeastListener(), this);
        pluginManager.registerEvents(new CPSChecker(), this);
        pluginManager.registerEvents(new RemoveHitCooldown(), this);
        pluginManager.registerEvents(new LastHitDetection(), this);
        pluginManager.registerEvents(new InventoryDetection(), this);
        pluginManager.registerEvents(new OldKnockback(this), this);
    }

    private void loadLocalizationFiles() {
        try {
            //U+00A7 = §
            String base = "/home/mcserver/shared_server_data/localization/";
            Localization.INSTANCE.loadLanguageFiles(Paths.get(base,"FFA"), "\u00A7");
            Localization.INSTANCE.loadLanguageFiles(Paths.get(base,"KitApi"), "\u00A7");
            Localization.INSTANCE.loadLanguageFiles(Paths.get(base,"Staff"), "\u00A7");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        this.getCommand("suicide").setExecutor(new SuicideCommand());
        this.getCommand("reloadmap").setExecutor(new ReloadMapCommand());
        new KitSettingsCommand(false);
    }
}
