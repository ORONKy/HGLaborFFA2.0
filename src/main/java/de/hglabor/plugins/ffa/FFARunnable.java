package de.hglabor.plugins.ffa;

import de.hglabor.plugins.ffa.world.ArenaManager;
import de.hglabor.utils.noriskutils.feast.Feast;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class FFARunnable extends BukkitRunnable {
    private final int resetDuration;
    private final World world;
    private int timer;

    public FFARunnable(World world, int resetDuration) {
        this.timer = resetDuration;
        this.resetDuration = resetDuration;
        this.world = world;
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() == 0) {
            return;
        }
        ArenaManager arenaManager = FFA.getArenaManager();
        timer--;
        if (timer == resetDuration / 2) {
            arenaManager.getFeast().spawn();
        }
        if (timer <= 0) {
            timer = resetDuration;
            arenaManager.setFeast(new Feast(FFA.getPlugin(), world).center(arenaManager.randomSpawn(50)).radius(20).timer(300).material(Material.GRASS_BLOCK));
            arenaManager.reloadMap();
        }
    }

    public int getTimer() {
        return timer;
    }
}

