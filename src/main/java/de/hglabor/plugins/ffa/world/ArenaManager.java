package de.hglabor.plugins.ffa.world;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import de.hglabor.plugins.ffa.FFA;
import de.hglabor.plugins.ffa.config.FFAConfig;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.HideUtils;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.config.KitMetaData;
import de.hglabor.plugins.kitapi.pvp.SkyBorder;
import de.hglabor.utils.noriskutils.ItemBuilder;
import de.hglabor.utils.noriskutils.WorldEditUtils;
import de.hglabor.utils.noriskutils.feast.Feast;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
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
        this.skyBorder = new SkyBorder(FFAConfig.getInteger("border.skyborder.damage"));
        this.center = new Location(world, 0, 0, 0);
        this.schematic = new File(FFA.getPlugin().getDataFolder().getAbsolutePath() + "/arena.schem");
        this.feast = new Feast(FFA.getPlugin(), world).center(randomSpawn(50)).damageItems(true).radius(20).timer(300).material(Material.GRASS_BLOCK);
        this.world.setTime(1000);
        this.world.setWeatherDuration(0);
        this.world.setThundering(false);
        this.world.getWorldBorder().setCenter(this.center);
        this.world.getWorldBorder().setDamageAmount(6);
        this.world.getWorldBorder().setSize(200);
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        this.world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        this.world.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
        this.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        this.copyMap();
    }

    public void prepareKitSelection(Player player) {
        if (player == null) {
            return;
        }
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);

        ffaPlayer.setStatus(FFAPlayer.Status.KITSELECTION);
        ffaPlayer.getKits().forEach(kit -> kit.onDisable(ffaPlayer));
        ffaPlayer.setKills(0);
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

        Location location = randomSpawn(40).clone().add(0, 20, 0);
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
        player.teleport(randomSpawn(50).clone().add(0, 1, 0));
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
        WorldEditUtils.pasteSchematic(world, center, schematic);
    }


    private void copyMap() {
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(this.world),
                BukkitAdapter.asBlockVector(new Location(this.world, this.size, 0, this.size)),
                BukkitAdapter.asBlockVector(new Location(this.world, -this.size, world.getMaxHeight(), -this.size)));

        BlockArrayClipboard blockArrayClipboard = new BlockArrayClipboard(region);
        blockArrayClipboard.setOrigin(BukkitAdapter.asBlockVector(this.center));

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, blockArrayClipboard, region.getMinimumPoint());
            forwardExtentCopy.setCopyingEntities(true);
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematic))) {
            writer.write(blockArrayClipboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location randomSpawn(int spread) {
        Random ran = new Random();
        int randomX = ran.nextInt(spread + spread) - spread;
        int randomZ = ran.nextInt(spread + spread) - spread;
        int highestY = getHighestBock(world, randomX, randomZ).getBlockY();
        return new Location(world, randomX, highestY, randomZ);
    }

    public Location getHighestBock(World world, int x, int z) {
        int i = 255;
        while (i > 0) {
            if (new Location(world, x, i, z).getBlock().getType() != Material.AIR)
                return new Location(world, x, i, z).add(0, 1, 0);
            i--;
        }
        return new Location(world, x, 1, z);
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

