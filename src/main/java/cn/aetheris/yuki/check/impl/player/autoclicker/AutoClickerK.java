package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.graphing.GraphUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AutoClickerK (Negatives)", type = CheckType.AUTOCLICKER, configName = "AutoClickerK")
public final class AutoClickerK extends Check implements PacketCheck {

    final List<Double> cpsSamples;
    final boolean unSupportClient = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
    double cps;
    int ticks;
    int vl;

    public AutoClickerK(PlayerData player) {
        super(player);
        cpsSamples = new ArrayList<>();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (isExempt(ExemptType.INTERACT) && !unSupportClient) {
                cps++;
            } else {
                buffer = 0;
                cps = 0;
            }
        } else if (isFlying(event.getPacketType())) {
            if (++ticks == 20) {
                if (cps > 9) {
                    cpsSamples.add(cps);
                    if (cpsSamples.size() == 10) {
                        final GraphUtil.GraphResult results = GraphUtil.getGraph(cpsSamples);

                        int negatives = results.negatives();
                        double average = cpsSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                        double deltaCps = Math.abs(average - cps);

                        if (negatives == 1 && deltaCps <= 1) {
                            if (++vl > 2) {
                                if (buffer++ > 3) {
                                    flagAndAlert("vl= " + vl
                                            + "\ndc= " + deltaCps
                                            + "\na=" + average
                                            + "\nc=" + cps);
                                }
                            } else {
                                vl = 0;
                                rewardBufferAndVL();
                                this.cpsSamples.clear();
                            }
                        }
                        this.ticks = 0;
                        this.cps = 0;
                    }
                }
            }
        }
    }
}
