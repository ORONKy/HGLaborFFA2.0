package de.hglabor.plugins.ffa.util;

import de.hglabor.plugins.ffa.FFA;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class HideUtils implements Listener {
    private static final HideUtils instance = new HideUtils();
    private final Map<Locale, BossBar> bossBars = new HashMap<>();

    private HideUtils() {
        for (Locale supportedLanguage : KitApi.getInstance().getSupportedLanguages()) {
            bossBars.put(supportedLanguage, Bukkit.createBossBar(Localization.INSTANCE.getMessage("bossBar.hideUtils", supportedLanguage), BarColor.RED, BarStyle.SOLID));
        }
    }

    public static HideUtils getInstance() {
        return instance;
    }

    public void hidePlayersInKitSelection(Player player) {
        if (player != null) {
            for (FFAPlayer playerInKitSelection : PlayerList.getInstance().getPlayersInKitSelection()) {
                Player playerToHide = playerInKitSelection.getPlayer();
                if (playerToHide != null) {
                    player.hidePlayer(FFA.getPlugin(), playerToHide);
                }
            }
        }
    }

    public void showPlayersInKitSelection(Player player) {
        if (player != null) {
            for (FFAPlayer playerInKitSelection : PlayerList.getInstance().getPlayersInKitSelection()) {
                Player playerToShow = playerInKitSelection.getPlayer();
                if (playerToShow != null) {
                    player.showPlayer(FFA.getPlugin(), playerToShow);
                }
            }
        }
    }

    public void hideToInGamePlayers(Player playerToHide) {
        bossBars.get(ChatUtils.locale(playerToHide.getUniqueId())).addPlayer(playerToHide);
        for (FFAPlayer playerInArena : PlayerList.getInstance().getPlayersInArena()) {
            Player player = playerInArena.getPlayer();
            player.hidePlayer(FFA.getPlugin(), playerToHide);
        }
    }

    public void makeVisibleToInGamePlayers(Player playerToShow) {
        bossBars.get(ChatUtils.locale(playerToShow.getUniqueId())).removePlayer(playerToShow);
        for (FFAPlayer playerInArena : PlayerList.getInstance().getPlayersInArena()) {
            Player player = playerInArena.getPlayer();
            player.showPlayer(FFA.getPlugin(), playerToShow);
        }
    }

    public void removeBossBars() {
        bossBars.values().forEach(BossBar::removeAll);
    }
}
