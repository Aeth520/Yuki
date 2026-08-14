package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a mitigation action (e.g. reset use item, shuffle hotbar) is about to execute.
 */
@Getter
public class MitigateEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public MitigateEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }
}