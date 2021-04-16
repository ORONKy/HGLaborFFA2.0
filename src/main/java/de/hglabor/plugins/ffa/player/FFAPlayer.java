package de.hglabor.plugins.ffa.player;
import de.hglabor.plugins.kitapi.player.KitPlayerImpl;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Locale;
import java.util.UUID;

public class FFAPlayer extends KitPlayerImpl implements ScoreboardPlayer {
    protected final String name;
    protected int kills;
    protected Status status;
    protected Scoreboard scoreboard;
    protected Objective objective;

    protected FFAPlayer(UUID uuid) {
        super(uuid);
        this.status = Status.KITSELECTION;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
    }

    public void increaseKills() {
        this.kills++;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    @Override
    public boolean isValid() {
        return isInArena();
    }

    public boolean isInKitSelection() {
        return status == Status.KITSELECTION;
    }

    public boolean isInArena() {
        return status == Status.ARENA;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public Objective getObjective() {
        return objective;
    }

    @Override
    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    @Override
    public Locale getLocale() {
        return ChatUtils.locale(uuid);
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public enum Status {
        KITSELECTION, ARENA, SPECTATOR,
    }
}
