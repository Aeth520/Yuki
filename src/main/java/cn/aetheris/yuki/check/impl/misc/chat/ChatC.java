package cn.aetheris.yuki.check.impl.misc.chat;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;

@CheckData(name = "ChatC", configName = "ChatC", type = CheckType.CHAT, description = "Chat Command Frequency")
public final class ChatC extends Check implements PacketCheck {

    private long minInterval;
    private long lastCommandTime = -1L;

    public ChatC(PlayerData player) {
        super(player);
    }

    @Override
    public void reload() {
        super.reload();
        this.minInterval = getConfig().getIntElse(getConfigName() + ".min-interval", 100);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_COMMAND) return;

        if (isExempt(ExemptType.TELEPORT)) return;

        final long now = System.currentTimeMillis();

        if (lastCommandTime != -1L) {
            final long diff = now - lastCommandTime;

            if (diff < minInterval) {
                if (flagAndAlert("interval= " + diff + "ms")) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            } else {
                rewardBufferAndVL();
            }
        }

        lastCommandTime = now;
    }
}
