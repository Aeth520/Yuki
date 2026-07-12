package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "ImpossibleI (MotionY)",
        configName = "ImpossibleI",
        description = "Impossible Motion",
        decay = 0.55,
        setback = 4,
        type = CheckType.IMPOSSIBLE,
        experimental = true)
public final class ImpossibleI extends Check implements PacketCheck {

    public ImpossibleI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {

            double motionY = player.lastDeltaY;
            double difference = motionY - 0.42;

            if (isExempt(ExemptType.CLIENT_VERSION
                    , ExemptType.FLYING
                    , ExemptType.BED
                    , ExemptType.VEHICLE
                    , ExemptType.VEHICLE_DIED
                    , ExemptType.SERVER_VERSION
                    , ExemptType.SEEM_WATER
                    , ExemptType.SLIGHTLY_TOUCHING_LIQUID
                    , ExemptType.LIQUID
                    , ExemptType.TELEPORT
                    , ExemptType.VOID
                    , ExemptType.SERVER_SENT_PULLBACK)) return;

            if (player.isGliding) return;

            if (GhostBlockUtil.isGhostBlock(player)) return;

            if (player.predictedVelocity.isKnockback()) return;

            if (player.uncertaintyHandler.isNearGlitchyBlock) return;

            if (player.getSetbackTeleportUtil().insideUnloadedChunk()) return;

            if (motionY != 0.4) return; 

            if (Math.abs(difference) < 1.7E-14) {
                if (buffer++ > 3)
                    if (flagAndAlert("d= " + difference + "\nb= " + buffer)) {
                        
                        
                    }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}