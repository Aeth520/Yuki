package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a player is about to be frozen.
 */
@Getter
public class FreezeEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public FreezeEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }
}