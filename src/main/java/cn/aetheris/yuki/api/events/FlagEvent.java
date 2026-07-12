package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import cn.aetheris.yuki.api.enums.CheckType;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


@Getter
public class FlagEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerAPI playerAPI;
    private final AbstractCheck check;
    private boolean cancelled;

    public FlagEvent(PlayerAPI playerAPI, AbstractCheck check) {
        super(true);
        this.playerAPI = playerAPI;
        this.check = check;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public PlayerAPI getPlayer() {
        return playerAPI;
    }

    public double getViolations() {
        return check.getViolations();
    }

    public CheckType getCheckType() {
        return check.getCheckType();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }


    public boolean isSetback() {
        return check.getViolations() > check.getSetbackVL();
    }

}