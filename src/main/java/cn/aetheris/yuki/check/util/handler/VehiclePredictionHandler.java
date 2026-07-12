package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.VehicleCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PositionUpdate;
import cn.aetheris.yuki.util.update.VehiclePositionUpdate;

public final class VehiclePredictionHandler extends Check implements VehicleCheck {
    public VehiclePredictionHandler(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void process(final VehiclePositionUpdate vehicleUpdate) {
        
        
        player.movementCheckRunner.processAndCheckMovementPacket(new PositionUpdate(vehicleUpdate.getFrom(), vehicleUpdate.getTo(), false, null, null, vehicleUpdate.isTeleport()));
    }
}
