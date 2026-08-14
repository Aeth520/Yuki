package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a player is unregistered from the anticheat system.
 */
@Getter
public class PlayerUnregisterEvent extends YukiCancellableEvent {
    private final PlayerAPI player;

    public PlayerUnregisterEvent(PlayerAPI player) {
        this.player = player;
    }

    @Override
    public AbstractCheck getCheck() {
        return null;
    }
}