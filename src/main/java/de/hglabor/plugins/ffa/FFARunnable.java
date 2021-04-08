package de.hglabor.plugins.ffa;

import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.LocationUtils;
import de.hglabor.plugins.ffa.world.ArenaManager;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.TimeConverter;
import de.hglabor.utils.noriskutils.feast.Feast;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FFARunnable extends BukkitRunnable {
    private final int resetDuration;
    private final World world;
    private final AtomicInteger timer;

    public FFARunnable(World world, int resetDuration) {
        this.timer = new AtomicInteger(resetDuration);
        this.resetDuration = resetDuration;
        this.world = world;
    }

    @Override
    public void run() {
        if (isRestartTime()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }
        if (Bukkit.getOnlinePlayers().size() == 0) {
            return;
        }
        ArenaManager arenaManager = FFA.getArenaManager();
        arenaManager.getSkyBorder().tick(PlayerList.getInstance().getPlayerEntitesInArena());
        if (timer.getAndDecrement() == resetDuration / 2) {
            arenaManager.getFeast().spawn();
        }
        announceMapReset(timer.get());
        if (timer.get() <= 0) {
            timer.set(resetDuration);
            arenaManager.setFeast(new Feast(FFA.getPlugin(), world).center(LocationUtils.getHighestBlock(world, (int) (world.getWorldBorder().getSize() / 4), 5)).damageItems(true).radius(20).timer(300).material(Material.GRASS_BLOCK));
            arenaManager.reloadMap();
        }
    }

    private void announceMapReset(int time) {
        if (time <= 10 || time % 300 == 0) {
            ChatUtils.broadcastMessage("ffa.mapReset", Map.of("time", TimeConverter.stringify(time)));
        }
    }

    private boolean isRestartTime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinut = Calendar.getInstance().get(Calendar.MINUTE);
        return currentHour == 3 && currentMinut == 0;
    }

    public int getTimer() {
        return timer.get();
    }
}

