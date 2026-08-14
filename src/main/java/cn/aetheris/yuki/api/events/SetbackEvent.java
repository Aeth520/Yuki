package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a player is about to be setback.
 */
@Getter
public class SetbackEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public SetbackEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }
}