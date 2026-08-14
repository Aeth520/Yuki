package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a player joins the anticheat system (data is registered).
 */
@Getter
public class PlayerRegisterEvent extends YukiCancellableEvent {
    private final PlayerAPI player;

    public PlayerRegisterEvent(PlayerAPI player) {
        this.player = player;
    }

    @Override
    public AbstractCheck getCheck() {
        return null;
    }
}