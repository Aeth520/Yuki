package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class CheckToggleEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerAPI player;
    private final AbstractCheck check;
    private final boolean enabled;

    public CheckToggleEvent(PlayerAPI player, AbstractCheck check, boolean enabled) {
        super(true);
        this.player = player;
        this.check = check;
        this.enabled = enabled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
