package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a movement prediction offset is calculated.
 * Refactored to extend {@link YukiCancellableEvent} directly.
 */
@Getter
public class PredictionEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;
    private final double offset;

    public PredictionEvent(PlayerAPI player, AbstractCheck check, double offset) {
        this.player = player;
        this.check = check;
        this.offset = offset;
    }
}