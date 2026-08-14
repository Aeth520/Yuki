package cn.aetheris.yuki.check.impl.movement.vehicle.nosaddle;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntityHorse;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "NoSaddleA (Controlling)", configName = "NoSaddleA", type = CheckType.VEHICLE, description = "Controlling an entity without a saddle.", decay = 0.25)
public final class NoSaddleA extends Check implements PacketCheck {

    public NoSaddleA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE
                && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {

            if (player.compensatedEntities.getSelf().getRiding() == null) {
                return;
            }

            if (player.compensatedEntities.getSelf().getRiding().isHorse()) {

                PacketEntityHorse horse = (PacketEntityHorse) player.compensatedEntities.getSelf().getRiding();

                if (horse == null) {
                    return;
                }

                double maxSpeed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.25 + 0.1;
                boolean saddle = horse.hasSaddle;
                double difference = maxSpeed - player.vehicleDeltaXZ;
                boolean invalid = !saddle && difference < 0.3 && player.vehicleDeltaXZ > 0.1;

                if (invalid && player.vehicleTicks > 5) {
                    if (buffer++ > 5) {
                        if (flagAndAlert("t= " + player.vehicleTicks
                                + "\ndxz= " + player.vehicleDeltaXZ)) {
                            event.setCancelled(true);
                            Bukkit.getScheduler().runTask(Yuki.getInstance(),
                                    () -> {
                                        if (player.bukkitPlayer != null) {
                                            player.bukkitPlayer.leaveVehicle();
                                        }
                                    });
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }
}
