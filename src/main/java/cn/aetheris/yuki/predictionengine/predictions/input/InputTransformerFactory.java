package cn.aetheris.yuki.predictionengine.predictions.input;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class InputTransformerFactory {

    private InputTransformerFactory() {
    }

    public static InputTransformer create(PlayerData player) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            return new ModernInputTransformer();
        }
        return new FloatInputTransformer();
    }
}
