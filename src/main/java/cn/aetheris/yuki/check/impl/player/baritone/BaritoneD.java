package cn.aetheris.yuki.check.impl.player.baritone;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BaritoneD (Small)", configName = "BaritoneD", decay = 0.75, description = "The player is using baritone (rotation)", setback = 8, type = CheckType.BARITONE)
public final class BaritoneD extends Check implements RotationCheck, PacketCheck {

    private boolean shouldCancel;
    private int breakTicks;

    public BaritoneD(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void process(RotationUpdate update) {
        if (breakTicks++ > 15) {
            return;
        }

        final float deltaYaw = update.getProcessor().getDeltaYaw();
        final float deltaPitch = update.getProcessor().getDeltaPitch();
        final boolean cinematic = update.isCinematic2() || update.isCinematic();
        final boolean invalid = deltaYaw < 0.005 && deltaPitch < 0.005 && deltaYaw > 0.0 && deltaPitch > 0.0 && !cinematic;
        if (invalid) {
            if (buffer++ > 3) {
                if (flagAndAlert("dy= " + deltaYaw + "\ndp= " + deltaPitch)) {
                    setbackIfAboveSetbackVL();
                    shouldCancel = true;
                    breakTicks = 0;
                }
            }
        } else {
            rewardBufferAndVL();
            shouldCancel = false;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.SWIMMING, ExemptType.LIQUID, ExemptType.TELEPORT)) {
                breakTicks = 0;
                shouldCancel = false;
            }
            if (player.isAttacking()) {
                breakTicks = 0;
                shouldCancel = false;
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);

            if (!event.isCancelled()) {
                event.setCancelled(shouldCancel);
            }

            if (dig.getAction() == DiggingAction.CANCELLED_DIGGING
                    || dig.getAction() == DiggingAction.FINISHED_DIGGING) {
                breakTicks = 0;
            }
        }
    }
}