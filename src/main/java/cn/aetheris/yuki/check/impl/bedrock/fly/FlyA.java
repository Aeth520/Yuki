package cn.aetheris.yuki.check.impl.bedrock.fly;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "FlyA (Air)", type = CheckType.BEDROCK, configName = "FlyA", decay = 1, experimental = true)
public class FlyA extends Check implements PacketCheck {

    public FlyA(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!player.isBedrockPlayer) {
            return;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!player.isBedrockPlayer) {
            return;
        }
    }
}