package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PunishEvent extends FlagEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PlayerAPI player;
    private final AbstractCheck check;
    private boolean cancelled;


    public PunishEvent(PlayerAPI player, AbstractCheck check) {
        super(player, check);
        this.player = player;
        this.check = check;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
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
}
