package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "KillAuraL (Frequency)", type = CheckType.KILLAURA, configName = "KillAuraL", decay = 0.8, description = "Attack frequency.", experimental = true)
public final class KillAuraL extends Check implements PacketCheck {

    private int movements;
    private int lastMovements;
    private int total;
    private int invalid;

    public KillAuraL(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final double cps = player.getCps();
                final boolean proper = cps > 7.2 && movements < 4 && lastMovements < 4 && !isExempt(ExemptType.CLIENT_ANTICHEAT);
                if (proper) {
                    final boolean flag = movements == lastMovements && shouldModifyPackets();
                    if (flag) {
                        ++invalid;
                    }
                    if (++total == 30) {
                        if (invalid > 28) {
                            if (buffer++ > 6) {
                                if (flagAndAlert("invalid= " + invalid + "\ncps= " + cps)) {
                                    player.mitigateDamage();
                                }
                            }
                        } else {
                            rewardBufferAndVL();
                        }
                        invalid = 0;
                        total = 0;
                    }
                }
                lastMovements = movements;
                movements = 0;
            }
        } else if (isFlying(event.getPacketType())) {
            ++movements;
        }
    }
}
