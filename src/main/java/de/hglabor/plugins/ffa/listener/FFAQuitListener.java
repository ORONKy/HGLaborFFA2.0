package de.hglabor.plugins.ffa.listener;

import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class FFAQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);
        ffaPlayer.getKits().forEach(kit -> kit.onDisable(ffaPlayer));
        if (ffaPlayer.isInCombat()) {
            player.setHealth(0);
        }
        PlayerList.getInstance().remove(player);
    }
}

