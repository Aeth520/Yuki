package cn.aetheris.yuki.check.impl.player.breaking.wrong;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "NoSwingBreakA (PACKET)", type = CheckType.BREAK, configName = "NoSwingBreakA", description = "Check for break noswing", decay = 0.85, setback = 8)
public final class NoSwingBreakA extends Check implements BlockBreakCheck {

    private boolean sentAnimation;
    private boolean sentBreak;

    public NoSwingBreakA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action != DiggingAction.CANCELLED_DIGGING) {
            sentBreak = true;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            sentAnimation = true;
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (sentBreak && !sentAnimation) {
                if (flagAndAlert()) {
                    event.setCancelled(true);
                    shuffleAboveSetbackVL();
                }
            }

            sentAnimation = sentBreak = false;
        }
    }
}