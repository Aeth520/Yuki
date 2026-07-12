package cn.aetheris.yuki.check.impl.player.badpackets.bad;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.BlockPlace;

@CheckData(name = "BadPacketsX (Small)",
        type = CheckType.BADPACKETS,
        configName = "BadPacketsX",
        decay = 0.65,
        description = "small pitch and high Yaw")
public final class BadPacketsX extends BlockPlaceCheck {

    private double lastDiffYaw;
    private double lastLastDiffYaw;

    public BadPacketsX(PlayerData player) {
        super(player);
    }


    @Override
    public void onBlockPlace(BlockPlace place) {
        if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.TELEPORT)) return;
        double diffYaw = MathUtil.getDistanceBetweenAngles(player.lastYaw, player.yaw);
        double diffPitch = MathUtil.getDistanceBetweenAngles(player.lastPitch, player.pitch);

        if (diffPitch > 1.5 && diffYaw > 30.0) {
            if (Math.abs(player.getDeltaX()) > 0.15 || Math.abs(player.getDeltaZ()) > 0.2) {
                if (lastDiffYaw > 30.0 && lastLastDiffYaw > 30.0) {
                    if (player.onGround && (double) (place.getPlacedBlockPos().y + 1) != player.getY()) {
                        return;
                    }
                    if (place.getFace().getFaceValue() == 1) {
                        return;
                    }
                    if (buffer++ > 4) {
                        if (flagAndAlert("diffYaw= " + diffYaw + "\ndiffP= " + diffPitch + "\nDX= " + player.getDeltaX() + "\nDY= " + player.getDeltaY() + "\nDZ= " + player.getDeltaZ())) {
                            rewardBufferAndVL();
                            if (violations > 5) place.setCancelled(true);
                            if (violations > 8) player.getSetbackTeleportUtil().executeViolationSetback();
                            player.mitigateDamage();

                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
        lastLastDiffYaw = lastDiffYaw;
        lastDiffYaw = diffYaw;
    }
}
