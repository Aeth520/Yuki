package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when the server is detected as lagging (TPS/MSPT threshold exceeded).
 */
@Getter
public class ServerLagEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final double tps;
    private final double mspt;

    public ServerLagEvent(PlayerAPI player, double tps, double mspt) {
        this.player = player;
        this.tps = tps;
        this.mspt = mspt;
    }

    @Override
    public AbstractCheck getCheck() {
        return null;
    }
}