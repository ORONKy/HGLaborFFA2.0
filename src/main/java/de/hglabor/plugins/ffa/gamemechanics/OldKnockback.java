package de.hglabor.plugins.ffa.gamemechanics;

import de.hglabor.plugins.ffa.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class OldKnockback implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        disableCooldown(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        disableCooldown(e.getPlayer());
    }

    private void disableCooldown(Player p) {
        p.setMetadata("oldKnockback", new FixedMetadataValue(Main.getPlugin(), ""));
    }
}
