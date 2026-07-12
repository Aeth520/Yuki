package cn.aetheris.yuki.check.impl.misc.spam;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

@CheckData(name = "SpamA (Client)", configName = "SpamA", description = "Sent Invalid Client Message", type = CheckType.SPAM, decay = 1.0)
public final class SpamA extends Check implements PacketCheck {

    private double buffer2;

    public SpamA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

        final WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);
        final String msg = message.getMessage();

        if (msg.contains("LiquidBounce Client | liquidbounce(.net) | CCBlueX on yt")) {
            punishment(event, "LiquidBounceClient" + "\nmessage= " + message.getMessage());
        } else if (msg.contains("Buy Minecraft Legit and stop using cracked servers") || msg.contains("get FDPClient")) {
            punishment(event, "FDPClient");
        } else if (msg.contains("SilenceFix Best The Config Free") || msg.contains("895367254")) {
            punishment(event, "SilenceFix");
        } else if (msg.equals("Meteor on Crack!")) {
            if (buffer2++ > 6) {
                punishment(event, "Meteor");
            } else {
                buffer2 = Math.max(buffer2 - getDecay(), 0);
            }
        } else if (msg.contains("get Flux @Flux.today")) {
            punishment(event, "FluxClient" + "\nmessage= " + message.getMessage());
        } else if (msg.contains("[欣欣公益") && msg.contains("免费获取点击右边的代码")) {
            if (buffer++ > 2) {
                if (flagAndAlert("reason= " + message.getMessage())) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    kickPlayer();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    private void punishment(PacketReceiveEvent event, String reason) {
        if (flagAndAlert("reason= " + reason)) {
            event.setCancelled(true);
            player.onPacketCancel();
            kickPlayer();
        }
    }
}