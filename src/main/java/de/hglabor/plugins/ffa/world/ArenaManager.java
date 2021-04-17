package de.hglabor.plugins.ffa.world;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import de.hglabor.plugins.ffa.FFA;
import de.hglabor.plugins.ffa.config.FFAConfig;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.HideUtils;
import de.hglabor.plugins.ffa.util.LocationUtils;
import de.hglabor.plugins.ffa.util.PasteAction;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.config.KitMetaData;
import de.hglabor.plugins.kitapi.pvp.SkyBorder;
import de.hglabor.utils.noriskutils.ItemBuilder;
import de.hglabor.utils.noriskutils.feast.Feast;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ArenaManager {
    private final World world;
    private final int size;
    private final File schematic;
    private final Location center;
    private final SkyBorder skyBorder;
    private Feast feast;

    public ArenaManager(World world, int mapSize) {
        this.world = world;
        this.size = mapSize;
        this.skyBorder = new SkyBorder(FFA.getPlugin(), FFAConfig.getInteger("border.skyborder.height"), FFAConfig.getInteger("border.skyborder.damage"));
        this.center = new Location(world, 0, 0, 0);
        this.schematic = new File(FFA.getPlugin().getDataFolder().getAbsolutePath() + "/arena.schem");
        this.feast = new Feast(FFA.getPlugin(), world).center(LocationUtils.getHighestBlock(world, 50, 0)).damageItems(true).radius(20).timer(300).material(Material.GRASS_BLOCK);
        this.world.setTime(1000);
        this.world.setWeatherDuration(0);
        this.world.setThundering(false);
        this.world.getWorldBorder().setCenter(this.center);
        this.world.getWorldBorder().setDamageAmount(6);
        this.world.getWorldBorder().setSize(mapSize*2);
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        this.world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        this.world.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
        this.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
    }

    public void prepareKitSelection(Player player) {
        if (player == null) {
            return;
        }
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);

        ffaPlayer.setStatus(FFAPlayer.Status.KITSELECTION);
        ffaPlayer.getKits().forEach(kit -> kit.onDisable(ffaPlayer));
        ffaPlayer.setKills(0);
        ffaPlayer.getLastHitInformation().clear();
        ffaPlayer.setKits(KitApi.getInstance().emptyKitList());
        //ffaPlayer.stopCombatTimer();
        ffaPlayer.resetKitAttributes();

        HideUtils.getInstance().hideToInGamePlayers(player);
        HideUtils.getInstance().showPlayersInKitSelection(player);

        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setFireTicks(0);
        player.setGliding(false);
        player.setGlowing(false);
        player.setTotalExperience(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(false);
        Arrays.stream(KitMetaData.values()).forEach(metaData -> player.removeMetadata(metaData.getKey(), FFA.getPlugin()));
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);

        player.closeInventory();
        player.getInventory().clear();

        KitApi.getInstance().getKitSelector().getKitSelectorItems().forEach(kitSelector -> player.getInventory().addItem(kitSelector));

        Location location = LocationUtils.getHighestBlock(world, (int) (world.getWorldBorder().getSize() / 2), 0).clone().add(0, 1, 0);
        player.teleport(location);
    }

    public void teleportToArena(Player player) {
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);
        ffaPlayer.setStatus(FFAPlayer.Status.ARENA);
        HideUtils.getInstance().hidePlayersInKitSelection(player);
        HideUtils.getInstance().makeVisibleToInGamePlayers(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.teleport(LocationUtils.getHighestBlock(world, (int) (world.getWorldBorder().getSize() / 2), 0).clone().add(0, 1, 0));
        this.giveArenaEquipment(player);
    }

    private void giveArenaEquipment(Player player) {
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(0, new ItemBuilder(Material.STONE_SWORD).setUnbreakable(true).build());
        inventory.setItem(1, new ItemBuilder(Material.COMPASS).setName("§cTracker").build());
        inventory.setItem(13, new ItemStack(Material.BOWL, 32));
        inventory.setItem(14, new ItemStack(Material.RED_MUSHROOM, 32));
        inventory.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 32));
        AtomicInteger itemCount = new AtomicInteger();
        for (AbstractKit kit : ffaPlayer.getKits()) {
            if (!kit.isUsingOffHand()) {
                for (ItemStack kitItem : kit.getKitItems()) {
                    inventory.setItem(1 + itemCount.incrementAndGet(), kitItem);
                }
            } else {
                player.getInventory().setItemInOffHand(kit.getMainKitItem());
            }
            kit.onEnable(PlayerList.getInstance().getKitPlayer(player));
        }

        IntStream.range(0, 31 - itemCount.get()).mapToObj(i -> new ItemStack(Material.MUSHROOM_STEW)).forEach(inventory::addItem);
    }


    public void reloadMap() {
        for (Entity entity : world.getNearbyEntities(new BoundingBox(size, 0, size, -size, 120, -size))) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }
        IAsyncWorldEdit awe = (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
        if (awe != null) {
            IPlayerEntry player = awe.getPlayerManager().getConsolePlayer();
            IThreadSafeEditSession tsSession = ((IAsyncEditSessionFactory) WorldEdit.getInstance().getEditSessionFactory()).getThreadSafeEditSession(new BukkitWorld(world), -1, null, player);
            awe.getBlockPlacer().performAsAsyncJob(tsSession, player, "loadWarGear:" + schematic.getName(), new PasteAction(schematic, center));
        }
    }

    public Feast getFeast() {
        return feast;
    }

    public void setFeast(Feast feast) {
        this.feast = feast;
    }

    public SkyBorder getSkyBorder() {
        return skyBorder;
    }
}

