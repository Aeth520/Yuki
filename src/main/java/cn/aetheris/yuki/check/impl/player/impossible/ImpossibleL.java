package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.Set;

@CheckData(name = "ImpossibleL (Died)",
        configName = "ImpossibleL",
        description = "Check interact when player died",
        decay = 0.85,
        type = CheckType.IMPOSSIBLE)

public final class ImpossibleL extends Check implements PacketCheck {

    private static final Set<PacketTypeCommon> PACKET_TYPES = Set.of(
            PacketType.Play.Client.INTERACT_ENTITY,
            PacketType.Play.Client.PLAYER_DIGGING,
            PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT,
            PacketType.Play.Client.HELD_ITEM_CHANGE,
            PacketType.Play.Client.CLICK_WINDOW,
            PacketType.Play.Client.CLOSE_WINDOW,
            PacketType.Play.Client.PLAYER_POSITION
    );
    private int deathTick;

    public ImpossibleL(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!player.compensatedEntities.getSelf().isDead) {
            deathTick = 0;
            return;
        }

        if (PACKET_TYPES.contains(event.getPacketType())) {
            deathTick++;
        }

        if (deathTick > 60 && buffer++ > 5) {
            if (alert("ticks= " + deathTick)) {
                player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                player.onPacketCancel();
            }
        } else {
            rewardBufferAndVL();
        }
    }
}