package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;

/**
 * Fired when a player finds an ore (XRay detection).
 * Refactored to extend {@link YukiCancellableEvent}.
 */
@Getter
public class PlayerFoundOreEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final String oreType;
    private final Vector3i position;

    public PlayerFoundOreEvent(PlayerAPI player, String oreType, Vector3i position) {
        this.player = player;
        this.oreType = oreType;
        this.position = position;
    }

    @Override
    public AbstractCheck getCheck() {
        return null;
    }
}