package de.hglabor.plugins.ffa.gamemechanics;

import com.google.common.collect.ImmutableMap;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Tracker implements Listener {

    @EventHandler
    public void onUseTracker(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Entity target = searchForCompassTarget(player);
        if (event.getMaterial() == Material.COMPASS) {
            if (target == null) {
                player.sendMessage(Localization.INSTANCE.getMessage("hglabor.tracker.noTarget", ChatUtils.getPlayerLocale(player)));
            } else {
                player.setCompassTarget(target.getLocation());
                player.sendMessage(Localization.INSTANCE.getMessage("hglabor.tracker.target", ImmutableMap.of("targetName", target.getName()), ChatUtils.getPlayerLocale(player)));
            }
        }
    }

    private Entity searchForCompassTarget(Player tracker) {
        List<Pair<Entity,Double>> pairs = new ArrayList<>();
        for (FFAPlayer ffaPlayer : PlayerList.getInstance().getPlayersInArena()) {
            Entity possibleTarget = Bukkit.getEntity(ffaPlayer.getUUID());
            if (possibleTarget == null)
                continue;
            if (tracker == possibleTarget)
                continue;
            double distanceBetween = getDistanceBetween(tracker, possibleTarget);
            if (distanceBetween > 30D) {
                pairs.add(Pair.of(possibleTarget,distanceBetween));
            }
        }
        Optional<Pair<Entity, Double>> target = pairs.stream().min(Comparator.comparingDouble(Pair::getRight));
        return target.isEmpty() ? null : target.get().getLeft();
    }

    private double getDistanceBetween(Entity player, Entity player2) {
        Location location = player.getLocation().clone();
        Location location2 = player2.getLocation().clone();
        location.setY(0);
        location2.setY(0);
        return location.distanceSquared(location2);
    }
}



