package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Arrays;

@CheckData(name = "KillAuraC (Lost)", type = CheckType.KILLAURA, configName = "KillAuraC", decay = 0.45, experimental = true)
public final class KillAuraC extends Check implements PacketCheck {

    private final long[] diffs = new long[8];
    private long lastFlag;
    private boolean isTran;
    private boolean isKa;
    private int diffIndex;
    private int samples;

    private double baseline = 50.0;
    private double deviation = 20.0;
    private long lastBaselineUpdate;

    public KillAuraC(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isTransaction(event.getPacketType())) {
            isTran = true;
        }
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            isKa = true;
        }
        if (isTickPacket(event.getPacketType())) {
            isKa = isTran = false;
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (player.hasAttackedSince(800L)) {
                if (isExempt(ExemptType.RESPAWN, ExemptType.TELEPORT) || player.sinceRiptideSpinTick < 60) {
                    rewardBufferAndVL();
                    return;
                }


                if (player.getTarget() == null || player.getLastTarget() == null) {
                    return;
                }

                double distanceNow = player.getTarget().getPossibleCollisionBoxes().distance(player.getBoundingBox());
                double distancePrev = player.getLastTarget().getPossibleCollisionBoxes().distance(player.getBoundingBox());
                if (distancePrev > distanceNow) {
                    if (isKa || isTran) {
                        final long now = time();
                        final long diff = Math.abs(player.getTransactionPing() - player.getKeepAlivePing());

                        if (diff > 60) {
                            player.sendTransaction();
                            diffs[diffIndex] = diff;
                            diffIndex = (diffIndex + 1) % diffs.length;
                            samples = Math.min(samples + 1, diffs.length);


                            if (now - lastBaselineUpdate > 5000 && samples > 3) {
                                double sum = 0;
                                for (int i = 0; i < samples; i++) sum += diffs[i];
                                baseline = sum / samples;

                                double varSum = 0;
                                for (int i = 0; i < samples; i++)
                                    varSum += Math.pow(diffs[i] - baseline, 2);
                                deviation = Math.sqrt(varSum / samples);

                                lastBaselineUpdate = now;
                            }


                            if (samples >= 5) {
                                double currentStd = 0;
                                if (deviation > 0) {
                                    double sum = 0;
                                    for (int i = 0; i < samples; i++)
                                        sum += Math.pow(diffs[i] - baseline, 2);
                                    currentStd = Math.sqrt(sum / samples);
                                }

                                boolean instantFlag = diff > (baseline + deviation * 3);

                                if (instantFlag) {
                                    if (time() - lastFlag < 500L) return;

                                    if (buffer++ > 8) {
                                        if (flagAndAlert("d= " + diff + String.format("\ns= %.1f", currentStd))) {
                                            rewardBufferAndVL();
                                            if (buffer > 16) player.mitigateDamage();
                                            Arrays.fill(diffs, 0);
                                            samples = 0;
                                            isKa = isTran = false;
                                        }
                                        player.sendTransaction();
                                        lastFlag = time();
                                    } else {
                                        rewardBufferAndVL();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
