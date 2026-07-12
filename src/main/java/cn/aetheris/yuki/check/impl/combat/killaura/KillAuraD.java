package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "KillAuraD (Protocol)",
        type = CheckType.KILLAURA,
        configName = "KillAuraD",
        description = "combat not following the vanilla protocol",
        decay = 0.785,
        experimental = true)
public final class KillAuraD extends Check implements PacketCheck {

    public long lastDoStuffTime;
    private float snapAngle;
    private long lastSnapTime;
    private double buffer2;
    private double buffer3;

    public KillAuraD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            if (flying.hasRotationChanged()) {
                float angle = MathUtil.getDistanceBetweenAngles(player.getLastYaw(), player.getYaw());
                if (angle > 90f && Math.abs(flying.getLocation().getPitch()) <= 60) {
                    this.lastSnapTime = time();
                    this.snapAngle = angle;
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digType = new WrapperPlayClientPlayerDigging(event);
            if (digType.getAction() == DiggingAction.FINISHED_DIGGING
                    || digType.getAction() == DiggingAction.CANCELLED_DIGGING) {
                lastDoStuffTime = time();
            }
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final double stuffDiff = time() - lastDoStuffTime;
                if (stuffDiff <= 100) {
                    if (buffer2++ > 3) {
                        if (flagAndAlert("(Stuff)\ndiff= " + stuffDiff)) {
                            player.mitigateDamage();
                            buffer2 -= getDecay();
                        }
                    } else {
                        buffer2 -= getDecay();
                    }
                }
                final double snapDiff = time() - lastSnapTime;
                if (snapDiff <= 150L) {
                    if (buffer3++ > 2) {
                        if (flagAndAlert("(Snap)\ndiff= " + snapDiff + "\nangle= " + Math.round(snapAngle * 1000) / 1000F)) {
                            player.mitigateDamage();
                            buffer3 -= getDecay();
                        }
                        lastSnapTime = 0L;
                    } else {
                        buffer3 -= getDecay();
                    }
                }
            }
        }
    }
}