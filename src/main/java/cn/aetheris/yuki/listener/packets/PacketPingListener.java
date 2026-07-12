package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.player.badpackets.BadPacketsK;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.util.message.ColorUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import net.kyori.adventure.text.Component;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketPingListener extends AbstractPacketListener {

    private static final Map<String, Long> playerPingExceededTime = new ConcurrentHashMap<>();

    
    public PacketPingListener() {
        super(PacketListenerPriority.LOWEST);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation transaction = new WrapperPlayClientWindowConfirmation(event);
            short id = transaction.getActionId();

            PlayerData data = getData(event.getUser());
            if (data == null) return;
            data.packetStateData.lastTransactionPacketWasValid = false;

            
            
            if (id <= 0 && data.addTransactionResponse(id)) {
                data.packetStateData.lastTransactionPacketWasValid = true;
                event.setCancelled(true);
            }
            
            if (!transaction.isAccepted()) {
                if (data.checkManager.getCheck(BadPacketsK.class).flagAndAlert("id= " + id)) {
                    event.setCancelled(true);
                    return;
                }
            }
            executeHighPingKick(data);
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong pong = new WrapperPlayClientPong(event);
            PlayerData data = getData(event.getUser());
            if (data == null) return;
            data.packetStateData.lastTransactionPacketWasValid = false;
            data.vehicleTicks = data.compensatedEntities.getSelf().getRiding() != null ? data.vehicleTicks + 1 : 0;

            int id = pong.getId();
            
            
            if (id == (short) id) {
                short shortID = ((short) id);
                if (data.addTransactionResponse(shortID)) {
                    data.packetStateData.lastTransactionPacketWasValid = true;
                    
                    event.setCancelled(true);
                }
            }
            executeHighPingKick(data);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
            short id = confirmation.getActionId();
            PlayerData data = getData(event.getUser());
            if (data == null) return;
            data.packetStateData.lastServerTransWasValid = false;
            
            if (id <= 0) {
                if (data.didWeSendThatTrans.remove(id)) {
                    data.packetStateData.lastServerTransWasValid = true;
                    data.transactionsSent.add(new Pair<>(id, System.nanoTime()));
                    data.lastTransactionSent.getAndIncrement();
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.PING) {
            WrapperPlayServerPing pong = new WrapperPlayServerPing(event);
            int id = pong.getId();
            PlayerData player = getData(event.getUser());
            if (player == null) return;
            player.packetStateData.lastServerTransWasValid = false;
            
            if (id == (short) id) {
                
                Short shortID = ((short) id);
                if (player.didWeSendThatTrans.remove(shortID)) {
                    player.packetStateData.lastServerTransWasValid = true;
                    player.transactionsSent.add(new Pair<>(shortID, System.nanoTime()));
                    player.lastTransactionSent.getAndIncrement();
                }
            }
        }
    }

    private void executeHighPingKick(PlayerData data) {
        if (!data.isBypass()
                && !(data.getBukkitPlayer() != null && data.getBukkitPlayer().hasPermission("yuki.exempt.highpingkick"))
                && PluginLoader.INSTANCE.getConfigManager().isHighPingKick()) {

            if (data.getExemptProcessor().isExempt(ExemptType.JOIN)) {
                return;
            }

            long currentPing = data.getTransactionPing();
            double maxPing = PluginLoader.INSTANCE.getConfigManager().getHighPingThreshold();

            if (currentPing > maxPing) {
                long currentTime = System.currentTimeMillis();
                playerPingExceededTime.putIfAbsent(data.getName(), currentTime);

                long lastExceededTime = playerPingExceededTime.get(data.getName());
                long diff = currentTime - lastExceededTime;
                DecimalFormat df = new DecimalFormat("#.#");
                String formatted = df.format(diff);
                if (diff >= (PluginLoader.INSTANCE.getConfigManager().getHighPingDuration() * 1000L)) {
                    data.disconnect(Component.text(ColorUtils.color(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.invalid-ping")
                            .replace("%ping%", data.getTransactionPing() + "")
                            .replace("%duration%", formatted))));
                }
            } else {
                playerPingExceededTime.remove(data.getName());
            }
        }
    }
}