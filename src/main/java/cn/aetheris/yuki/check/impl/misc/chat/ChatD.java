package cn.aetheris.yuki.check.impl.misc.chat;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

import java.util.Optional;

@CheckData(name = "ChatD", configName = "ChatD", type = CheckType.CHAT, description = "Invalid Chat Signature")
public final class ChatD extends Check implements PacketCheck {

    public ChatD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_19)) return;

        if (isExempt(ExemptType.TELEPORT)) return;

        final WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);
        final Optional<MessageSignData> signDataOpt = message.getMessageSignData();

        if (signDataOpt.isEmpty()) {
            if (flagAndAlert("reason= missing signature")) {
                event.setCancelled(true);
                player.onPacketCancel();
            }
            return;
        }

        final MessageSignData signData = signDataOpt.get();
        final SaltSignature saltSignature = signData.getSaltSignature();
        final byte[] signature = saltSignature.getSignature();

        if (signature == null || signature.length == 0) {
            if (flagAndAlert("reason= empty signature")) {
                event.setCancelled(true);
                player.onPacketCancel();
            }
        }
    }
}
