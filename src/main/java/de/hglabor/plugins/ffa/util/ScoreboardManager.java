package de.hglabor.plugins.ffa.util;

import de.hglabor.plugins.ffa.Main;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.kitapi.config.KitApiConfig;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.kits.CopyCatKit;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.TimeConverter;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public final class ScoreboardManager extends BukkitRunnable {

    public static void setBasicScoreboardLayout(ScoreboardPlayer scoreboardPlayer) {
        int kitAmount = KitApiConfig.getInstance().getInteger("kit.amount");
        int lowestPosition = 7;
        int highestPosition = lowestPosition + kitAmount;
        ScoreboardFactory.addEntry(scoreboardPlayer, "reset", Localization.INSTANCE.getMessage("scoreboard.mapReset", scoreboardPlayer.getLocale()), "", highestPosition + 3);
        ScoreboardFactory.addEntry(scoreboardPlayer, "resetValue", TimeConverter.stringify(Main.getFFARunnable().getTimer()), "", highestPosition + 2);
        ScoreboardFactory.addEntry(scoreboardPlayer, String.valueOf(highestPosition + 1), "", "", highestPosition + 1);
        if (kitAmount == 1) {
            ScoreboardFactory.addEntry(scoreboardPlayer, "kitValue" + 1, "Kit: None", "", highestPosition);
        } else if (kitAmount > 1) {
            for (int i = highestPosition; i > lowestPosition; i--) {
                ScoreboardFactory.addEntry(scoreboardPlayer, "kitValue" + (i - lowestPosition), "Kit" + (i - lowestPosition) + ": None", "", i);
            }
        }
        ScoreboardFactory.addEntry(scoreboardPlayer, "killsValue", "Kills: 0", "", lowestPosition);
        ScoreboardFactory.addEntry(scoreboardPlayer, "6", "", "", 6);
        ScoreboardFactory.addEntry(scoreboardPlayer, "players", Localization.INSTANCE.getMessage("scoreboard.players", scoreboardPlayer.getLocale()), "", 5);
        ScoreboardFactory.addEntry(scoreboardPlayer, "playersValue", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), "", 4);
        ScoreboardFactory.addEntry(scoreboardPlayer, "3", "", "", 3);
    }

    @Override
    public void run() {
        for (FFAPlayer ffaPlayer : PlayerList.getInstance().getPlayers()) {
            ScoreboardFactory.updateEntry(ffaPlayer, "playersValue", SPACE() + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), "");
            ScoreboardFactory.updateEntry(ffaPlayer, "killsValue", ChatColor.AQUA + "" + ChatColor.BOLD + "Kills: " + ChatColor.RESET + ffaPlayer.getKills(), "");
            ScoreboardFactory.updateEntry(ffaPlayer, "resetValue", TimeConverter.stringify(Main.getFFARunnable().getTimer()), "");

            boolean kitDisabled = ffaPlayer.areKitsDisabled();

            //could possibly be none -> name check
            if (KitApiConfig.getInstance().getInteger("kit.amount") > 0) {
                int index = 1;
                for (AbstractKit kit : ffaPlayer.getKits()) {
                    if (kit.equals(CopyCatKit.INSTANCE)) {
                        AbstractKit copiedKit = ffaPlayer.getKitAttribute(CopyCatKit.INSTANCE);
                        ScoreboardFactory.updateEntry(ffaPlayer,
                                "kitValue" + index, ChatColor.BLUE + "" + ChatColor.BOLD + "Kit" + (index == 1 ? "" : index) + ": " + ChatColor.RESET +
                                        (kitDisabled ? ChatColor.STRIKETHROUGH : ChatColor.RESET) + kit.getName() +
                                        "(" + (copiedKit != null ? ((AbstractKit) ffaPlayer.getKitAttribute(CopyCatKit.INSTANCE)).getName() : "None") + ")", "");
                    } else {
                        ScoreboardFactory.updateEntry(ffaPlayer, "kitValue" + index, ChatColor.BLUE + "" + ChatColor.BOLD + "Kit" + (KitApiConfig.getInstance().getInteger("kit.amount") == 1 ? "" : index) + ": " + ChatColor.RESET + (kitDisabled ? ChatColor.STRIKETHROUGH : ChatColor.RESET) + kit.getName(), "");
                    }
                    index++;
                }
            }
        }
    }

    private String SPACE() {
        return " ";
    }
}
