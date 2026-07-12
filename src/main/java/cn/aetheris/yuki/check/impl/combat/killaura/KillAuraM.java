package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;

import java.util.List;

@CheckData(
        name = "KillAuraM (Behavior)",
        configName = "KillAuraM",
        description = "Detects abnormal attack patterns through latency analysis",
        type = CheckType.KILLAURA,
        experimental = true,
        decay = 0.645
)
public class KillAuraM extends Check implements PacketCheck {

    private final Watch checkPing = new Watch();
    private double buffer2;
    private int buffer3;

    private PacketEntity target;
    private boolean inited = false;
    private long avgPing;
    private long avgPingDistance = -1;
    private SimpleCollisionBox targetLastPos = null;

    private double pingOffset;
    private double bufferOffset;

    private long lastFlag;
    private long lastFlag2;

    public KillAuraM(PlayerData player) {
        super(player);
    }

    @Override
    public void reload() {
        pingOffset = getConfig().getDouble(getConfigName() + ".ping-offset");
        bufferOffset = getConfig().getDouble(getConfigName() + ".buffer-multiplier");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isTransaction(event.getPacketType())) {
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_17) && event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION && !new WrapperPlayClientWindowConfirmation(event).isAccepted()) {
                return;
            }

            while (player.getLongTermPingList().size() > 200) {
                player.getLongTermPingList().remove(0);
            }
            while (player.getPingList().size() > 10) {
                player.getPingList().remove(0);
            }

            if (player.getTranDelay() != -1 && player.getTranDelay() != 0) {
                long diff = Math.abs(time() - player.getTranDelay());
                player.getLongTermPingList().add(diff);
                player.getPingList().add(diff);
            }
            player.setTranDelay(time());

        }
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (checkPing.hasTimeElapsed(20000)) {
                buffer3--;
                if (buffer3 < 0) buffer3 = 0;
                checkPing.reset();
            }

            List<Long> longTermPingList1 = player.getLongTermPingList();
            if (player.hasAttackedSince(1000L)) {
                
                if (longTermPingList1.size() > 100 && inited) {
                    long currentAvgPing = (long) MathUtil.getAverage(player.getPingList());
                    double maxOffsetPing = (5 + currentAvgPing / 7.0) * pingOffset;
                    
                    long offset = currentAvgPing - avgPing;
                    if (offset >= maxOffsetPing) {
                        player.sendTransaction();
                        if (buffer++ > 30) {
                            buffer *= 0.85;
                            if (time() - lastFlag < 700L) {
                                if (buffer3++ > 4 * bufferOffset) {
                                    buffer3 -= 2;
                                    if (flagAndAlert("(Offset)\no= " + offset)) {
                                        player.mitigateDamage();
                                        player.sendTransaction();
                                    }
                                    lastFlag = time();
                                }
                            }
                        }
                    } else {
                        buffer = 0;
                    }
                    if (target != player.getTarget()) {
                        target = player.getTarget();
                        buffer = 0;
                        avgPingDistance = -1;
                    }

                    if (target != null) {

                        if (target.isDead) {
                            return;
                        }

                        if (targetLastPos != null) {
                            double distanceNow = target.getPossibleCollisionBoxes().distance(player.getBoundingBox());
                            double distancePrev = targetLastPos.distance(player.getBoundingBox());
                            if (distancePrev > distanceNow) {
                                
                                avgPingDistance = (long) MathUtil.getAverage(player.getPingList());
                            } else {
                                if (avgPingDistance != -1) {
                                    long offset2 = (long) (MathUtil.getAverage(player.getPingList()) - avgPing);
                                    
                                    if (offset2 > maxOffsetPing) {
                                        player.sendTransaction();
                                        if (time() - lastFlag2 < 1000L) {
                                            if ((buffer2++) + buffer * 0.2 > 30 * 0.85) {
                                                if (flagAndAlert("(Average)\no= " + offset2)) {
                                                    player.mitigateDamage();
                                                    player.sendTransaction();
                                                }
                                            }
                                            lastFlag2 = time();
                                        }
                                    } else {
                                        buffer2 *= 0.93;
                                        if (buffer2 < 0) buffer2 = 0;
                                    }
                                }
                            }
                        }
                        targetLastPos = target.getPossibleCollisionBoxes();
                    } else {
                        targetLastPos = null;
                    }
                }
                return;
            }
            
            if (longTermPingList1.size() > 60) {
                inited = true;
                avgPing = (long) MathUtil.getAverage(longTermPingList1);
            }
        }
    }
}