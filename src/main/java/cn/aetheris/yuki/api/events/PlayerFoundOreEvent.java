package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.PlayerAPI;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerFoundOreEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerAPI player;
    private final String oreType;
    private final Vector3i position;
    private boolean cancelled;

    public PlayerFoundOreEvent(PlayerAPI player, String oreType, Vector3i position) {
        super(true);
        this.player = player;
        this.oreType = oreType;
        this.position = position;
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
