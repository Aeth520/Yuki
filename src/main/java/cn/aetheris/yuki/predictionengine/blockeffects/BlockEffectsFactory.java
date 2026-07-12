package cn.aetheris.yuki.predictionengine.blockeffects;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class BlockEffectsFactory {
    public static BlockEffectsResolver create(PlayerData player) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_6)) {
            return new BlockEffectsResolverV1_21_6();
        }
        return new DefaultBlockEffectsResolver();
    }
}
