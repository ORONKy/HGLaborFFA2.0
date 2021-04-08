package de.hglabor.plugins.ffa.util;

import de.hglabor.plugins.kitapi.kit.kits.GladiatorKit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class LocationUtils {
    private final static Random random = new Random();
    private final static List<Material> skipBlocks = Arrays.asList(
            Material.RED_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS);

    private LocationUtils() {
    }

    public static Location getHighestBlock(World world, int spread, int tryCounter) {
        int seaLevel = world.getSeaLevel();
        int randomX = random.nextInt(spread + spread) - spread;
        int randomZ = random.nextInt(spread + spread) - spread;

        if (tryCounter > 8) {
            int highestY = world.getHighestBlockYAt(randomX, randomZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            return new Location(world, randomX, highestY, randomZ);
        }

        for (int i = seaLevel + 20; i > seaLevel - 10; i--) {
            Block block = world.getBlockAt(randomX, i, randomZ);
            Location blockLoc = block.getLocation();
            Material type = block.getType();
            if (type.equals(GladiatorKit.INSTANCE.getMaterial()) || skipBlocks.contains(type)) {
                continue;
            }
            if (block.isSolid() && block.getRelative(BlockFace.DOWN).isSolid()) {
                if (!blockLoc.clone().add(0, 1, 0).getBlock().isSolid() && !blockLoc.clone().add(0, 2, 0).getBlock().isSolid()) {
                    return block.getLocation();
                }
            }
        }
        return getHighestBlock(world, spread, tryCounter);
    }
}
