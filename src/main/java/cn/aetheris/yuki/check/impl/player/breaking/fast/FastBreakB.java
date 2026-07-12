package cn.aetheris.yuki.check.impl.player.breaking.fast;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "FastBreakB (Instant)", configName = "FastBreakB", description = "Instant Block Break", decay = 0.25, setback = 1, type = CheckType.BREAK)
public final class FastBreakB extends Check implements PacketCheck {
    boolean tickStarted;

    public FastBreakB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (isExempt(ExemptType.CLIENT_VERSION)) return;

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tickStarted = false;
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging type = new WrapperPlayClientPlayerDigging(event);
            switch (type.getAction()) {
                case START_DIGGING:
                    tickStarted = true;
                    break;
                case FINISHED_DIGGING:
                    if (tickStarted) {
                        if (shouldModifyPackets() && flagWithSetback()) {
                            alert("");
                            event.setCancelled(true);
                            player.onPacketCancel();
                        }
                    }
                    rewardVL();
            }
        } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            tickStarted = false;
        }
    }
}
