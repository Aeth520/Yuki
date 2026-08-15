package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Yuki cancellable events, inspired by NekoAntiCheat's NekoCancellableEvent.
 * Integrates Intave's {@link Reaction} system for granular control while maintaining
 * backward compatibility with {@link Cancellable}.
 */
public abstract class YukiCancellableEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    @Setter
    private Reaction reaction = Reaction.INTERRUPT_AND_REPORT;

    protected YukiCancellableEvent() {
        super(true);
    }

    protected YukiCancellableEvent(boolean isAsync) {
        super(isAsync);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    // --- Legacy Cancellable compatibility ---

    @Override
    public boolean isCancelled() {
        return reaction != Reaction.INTERRUPT_AND_REPORT;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.reaction = cancel ? Reaction.IGNORE : Reaction.INTERRUPT_AND_REPORT;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public abstract PlayerAPI getPlayer();

    public abstract AbstractCheck getCheck();
}