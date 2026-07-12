package cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "NoSaddleB", type = CheckType.ENTITY)
public final class NoSaddleB extends Check implements PostPredictionCheck {
    public NoSaddleB(PlayerData player) {
        super(player);
    }

    public void rewardPlayer() {
        rewardVL();
    }
}
