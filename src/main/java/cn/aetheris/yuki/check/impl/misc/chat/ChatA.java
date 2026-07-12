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

@CheckData(name = "ChatA", configName = "ChatA", type = CheckType.CHAT, description = "Invalid Chat Message Length")
public final class ChatA extends Check implements PacketCheck {

    private int maxLength;

    public ChatA(PlayerData player) {
        super(player);
    }

    @Override
    public void reload() {
        super.reload();
        this.maxLength = getConfig().getIntElse(getConfigName() + ".max-length", 256);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

        if (isExempt(ExemptType.TELEPORT)) return;

        final WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);
        final String msg = message.getMessage();

        if (msg.length() > maxLength) {
            if (flagAndAlert("length= " + msg.length())) {
                event.setCancelled(true);
                player.onPacketCancel();
                kickPlayer();
            }
        }
    }
}
