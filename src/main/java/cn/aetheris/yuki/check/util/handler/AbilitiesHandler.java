package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;




@CheckData(utilityClass = true)
public final class AbilitiesHandler extends Check implements PacketCheck {

    private boolean lastSentPlayerCanFly = false;
    private int maxFlyingPing = 1000;

    public AbilitiesHandler(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES) {
            WrapperPlayClientPlayerAbilities abilities = new WrapperPlayClientPlayerAbilities(event);
            player.isFlying = abilities.isFlying() && player.canFly;
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_ABILITIES) {
            WrapperPlayServerPlayerAbilities abilities = new WrapperPlayServerPlayerAbilities(event);

            player.sendTransaction();

            if (lastSentPlayerCanFly && !abilities.isFlightAllowed()) {
                int noFlying = player.lastTransactionSent.get();
                if (maxFlyingPing != -1) {
                    player.runNettyTaskInMs(() -> {
                        if (player.lastTransactionReceived.get() < noFlying) {
                            player.getSetbackTeleportUtil().executeViolationSetback();
                            LogUtils.setback("&b " + player.getName() + "&7 has been setback for high ping &7(&bOutFlying&7)");
                        }
                    }, maxFlyingPing);
                }
            }

            lastSentPlayerCanFly = abilities.isFlightAllowed();

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                player.canFly = abilities.isFlightAllowed();
                player.isFlying = abilities.isFlying();
            });

        }
    }

    @Override
    public void reload() {
        super.reload();
        maxFlyingPing = getConfig().getIntElse("function.limit.ping.outflying", 1000);
    }
}
