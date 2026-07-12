package cn.aetheris.yuki.check.impl.misc.chat;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

@CheckData(name = "ChatB", configName = "ChatB", type = CheckType.CHAT, description = "Chat Message Frequency")
public final class ChatB extends Check implements PacketCheck {

    private long minInterval;
    private long lastChatTime = -1L;

    public ChatB(PlayerData player) {
        super(player);
    }

    @Override
    public void reload() {
        super.reload();
        this.minInterval = getConfig().getIntElse(getConfigName() + ".min-interval", 200);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

        if (isExempt(ExemptType.TELEPORT)) return;

        final long now = System.currentTimeMillis();

        if (lastChatTime != -1L) {
            final long diff = now - lastChatTime;

            if (diff < minInterval) {
                if (flagAndAlert("interval= " + diff + "ms")) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            } else {
                rewardBufferAndVL();
            }
        }

        lastChatTime = now;
    }
}
