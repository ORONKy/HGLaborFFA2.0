package de.hglabor.plugins.ffa.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class FFAMoveListener implements Listener {

    double x = 0.5;
    double y = 2;
    double z = 0.5;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SPONGE) {

            Vector v = new Vector(x, y, z).normalize().setY(y + 1);
            player.setVelocity(v);

        }
    }
}
