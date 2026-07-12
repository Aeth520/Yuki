package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;

@CheckData(name = "ImpossibleD (Vehicle)", configName = "ImpossibleD", description = "Checks for a common exploit in disabler modules.", experimental = true, type = CheckType.IMPOSSIBLE)
public final class ImpossibleD extends Check implements PacketCheck {

    private static final float MOVEMENT_THRESHOLD = 0.98f;
    private long lastFlag;
    private long lastFlag2;

    public ImpossibleD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {

            final WrapperPlayClientSteerVehicle packet = new WrapperPlayClientSteerVehicle(event);

            if (player.getBukkitPlayer() != null
                    && player.getBukkitPlayer().getVehicle() != null
                    && player.getBukkitPlayer().getVehicle().getType().name().contains("LLAMA")) {
                return;
            }

            if (!player.inVehicle()) {
                if (time() - lastFlag < 800L) {
                    return;
                }

                if (buffer++ > 4) {
                    if (flagAndAlert("client= true"
                            + "\nserver= " + player.inVehicle())) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                    lastFlag = time();
                }
            }

            float forwardMovement = Math.abs(packet.getForward());
            float sidewaysMovement = Math.abs(packet.getSideways());

            if (forwardMovement > MOVEMENT_THRESHOLD || sidewaysMovement > MOVEMENT_THRESHOLD) {
                if (time() - lastFlag2 < 800L) {
                    return;
                }
                if (flagAndAlert("forwards= " + packet.getForward() + "\nsideways= " + packet.getSideways())
                ) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    lastFlag2 = time();
                }
            }
        }
    }
}