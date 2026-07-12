package cn.aetheris.yuki.check.impl.player.baritone;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "BaritoneB", decay = 0.15, description = "The player is using baritone (rotation)", setback = 8, type = CheckType.BARITONE)
public final class BaritoneB extends Check implements PacketCheck {
    float lastDeltaPitch;

    public BaritoneB(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digType = new WrapperPlayClientPlayerDigging(event);

            if (digType.getAction() != DiggingAction.CANCELLED_DIGGING
                    || digType.getAction() != DiggingAction.FINISHED_DIGGING) return;

            final double pitch = player.pitch;
            final double lastPitch = player.lastPitch;

            if (Math.abs(pitch) == 90F) return;

            final float deltaPitch = (float) Math.abs(pitch - lastPitch);

            final float delta = Math.abs(lastDeltaPitch - deltaPitch);

            if (delta < .005 && delta > 0) {
                if (flagAndAlert("delta=" + delta) && shouldModifyPackets()) {
                    setbackIfAboveSetbackVL();
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
            lastDeltaPitch = deltaPitch;
        } else {
            rewardVL();
        }
    }
}