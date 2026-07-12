package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "ScaffoldF (Down)",
        configName = "ScaffoldF",
        decay = 0.65,
        description = "Invalid rotation like scaffold")
public class ScaffoldF extends BlockPlaceCheck {

    private boolean isBridging;
    private int placed;
    private int aacPlaced;

    private boolean cancel;

    public ScaffoldF(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.isBlock()) {
            isBridging = isBridging(place);
            placed++;
            if (cancel) {
                place.setCancelled(true);
                cancel = false;
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!isExempt(ExemptType.TELEPORT, ExemptType.RESPAWN)) {
                isBridging = false;
            }
        }
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (!isBridging) {
            aacPlaced = 0;
            buffer = 0;
            return;
        }

        if (!player.isMoving()) {
            aacPlaced = 0;
            buffer = 0;
            return;
        }

        final double deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();
        final double deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();
        final boolean isSneaking = player.isSneaking();

        if (!isSneaking && deltaPitch > 0 && aacPlaced != placed) {
            if (deltaYaw == 0F) {
                buffer++;
                if (deltaPitch > 0.05F) buffer++;
                if (deltaPitch < 0.5F) buffer++;
            }

            aacPlaced = placed;
            if (buffer > 8) {
                if (flagAndAlert("placed= " + placed)) {
                    if (getViolations() > 4) cancel = true;
                    player.mitigateDamage();
                    rewardBufferAndVL();
                }
            }
        } else if (placed - aacPlaced > 10 && buffer > 0) {
            rewardBufferAndVL();
            aacPlaced = placed;
        }
    }
}
