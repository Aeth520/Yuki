package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a punishment (ban/kick/etc.) is about to be executed for a player.
 * Refactored to extend {@link YukiCancellableEvent} directly.
 */
@Getter
public class PunishEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public PunishEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }
}