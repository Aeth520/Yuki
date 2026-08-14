package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a ghost block is detected and about to be mitigated.
 */
@Getter
public class GhostBlockEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public GhostBlockEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }
}