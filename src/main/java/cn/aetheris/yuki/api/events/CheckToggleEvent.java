package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a check is enabled or disabled for a player.
 * Refactored to extend {@link YukiCancellableEvent}.
 */
@Getter
public class CheckToggleEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;
    private final boolean enabled;

    public CheckToggleEvent(PlayerAPI player, AbstractCheck check, boolean enabled) {
        this.player = player;
        this.check = check;
        this.enabled = enabled;
    }
}