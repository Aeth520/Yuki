package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PositionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PositionUpdate;

public final class PredictionHandler extends Check implements PositionCheck {
    public PredictionHandler(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPositionUpdate(final PositionUpdate positionUpdate) {
        if (!player.inVehicle()) {
            player.movementCheckRunner.processAndCheckMovementPacket(positionUpdate);
        }
    }
}
