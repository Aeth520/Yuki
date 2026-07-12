package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCooldown;

public final class PacketPlayerCooldown extends AbstractPacketListener {

    public PacketPlayerCooldown() {
        super(PacketListenerPriority.HIGH);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_COOLDOWN) {
            WrapperPlayServerSetCooldown cooldown = new WrapperPlayServerSetCooldown(event);

            PlayerData player = getData(event.getUser());
            if (player == null) return;

            int lastTransactionSent = player.lastTransactionSent.get();

            if (cooldown.getCooldownTicks() == 0) { 
                player.latencyUtils.addRealTimeTask(lastTransactionSent + 1, () -> player.checkManager.getCompensatedCooldown().removeCooldown(cooldown.getCooldownGroup()));
            } else {
                player.latencyUtils.addRealTimeTask(lastTransactionSent, () -> player.checkManager.getCompensatedCooldown().addCooldown(cooldown.getCooldownGroup(),
                        cooldown.getCooldownTicks(), lastTransactionSent));
            }
        }
    }
}
