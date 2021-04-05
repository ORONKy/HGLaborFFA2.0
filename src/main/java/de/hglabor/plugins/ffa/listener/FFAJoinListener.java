package de.hglabor.plugins.ffa.listener;

import de.hglabor.plugins.ffa.FFA;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.ScoreboardManager;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static de.hglabor.utils.localization.Localization.t;


public class FFAJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);
        PlayerList.getInstance().add(ffaPlayer);
        player.sendTitle(
                t("hglabor.ffa.joinTitle", ffaPlayer.getLocale()),
                t("hglabor.ffa.lowerJoinTitle", ffaPlayer.getLocale()),
                20, 20, 20);
        ScoreboardFactory.create(ffaPlayer);
        ScoreboardManager.setBasicScoreboardLayout(ffaPlayer);
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> ScoreboardFactory.addPlayerToNoCollision(player, PlayerList.getInstance().getPlayer(onlinePlayer)));
        FFA.getArenaManager().prepareKitSelection(player);
    }
}

