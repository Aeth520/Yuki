package cn.aetheris.yuki.check.impl.player.breaking.fast;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.packets.patch.ResyncWorldUtil;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "FastBreakC (Packet)", configName = "FastBreakC", description = "Invalid break diff", setback = 13, experimental = true, type = CheckType.BREAK)
public final class FastBreakC extends Check implements PacketCheck {

    int ticks;
    int stage;
    int count;
    long lastPacketDelta;

    public FastBreakC(PlayerData player) {
        super(player);
        buffer = 0.0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

        if (isFlying(event.getPacketType())) {
            if (stage == 2) {
                buffer -= Math.min(buffer + 1.0, 0.01);
            }
            if (stage == 1) {
                ++ticks;
                stage = 2;
            }
            stage = stage == 2 ? 3 : 0;
        }
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            buffer = 0;
            rewardVL();
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digType = new WrapperPlayClientPlayerDigging(event);
            ++count;
            long packetDelta = time() - lastPacketDelta;
            if (packetDelta >= 1000L) {
                lastPacketDelta = time();
                if (count >= 60) {
                }
                count = 0;
            }
            if (digType.getAction() == DiggingAction.FINISHED_DIGGING) {
                stage = 1;
                buffer -= 1.0E-4;
            }
            if (digType.getAction() != DiggingAction.START_DIGGING && digType.getAction() != DiggingAction.FINISHED_DIGGING) {
                buffer = 0;
            } else if (buffer++ > 3) {
                if (flagAndAlert("t= Nuker"
                        + "\ndt= " + digType.getAction()
                        + "\nx= " + player.yaw
                        + "\ny= " + player.pitch) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    ResyncWorldUtil.resyncPosition(player, digType.getBlockPosition(), digType.getSequence());
                    setbackIfAboveSetbackVL();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}