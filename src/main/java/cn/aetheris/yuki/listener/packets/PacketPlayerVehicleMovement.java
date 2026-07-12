package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;

public final class PacketPlayerVehicleMovement extends AbstractPacketListener {

    public PacketPlayerVehicleMovement() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.VEHICLE_MOVE) {
            return;
        }

        final PlayerData player = getData(event.getUser());

        if (player == null) {
            return;
        }

        final WrapperPlayClientVehicleMove move = new WrapperPlayClientVehicleMove(event);

        player.vehicleX = move.getPosition().getX();
        player.vehicleY = move.getPosition().getY();
        player.vehicleZ = move.getPosition().getZ();

        player.vehicleDeltaX = Math.abs(player.vehicleX - player.lastVehicleX);
        player.vehicleDeltaY = Math.abs(player.vehicleY - player.lastVehicleY);
        player.vehicleDeltaZ = Math.abs(player.vehicleZ - player.lastVehicleZ);

        player.lastVehicleX = player.vehicleX;
        player.lastVehicleY = player.vehicleY;
        player.lastVehicleZ = player.vehicleZ;

        player.vehicleDeltaXZ = MathUtil.hypot(player.vehicleDeltaX, player.vehicleDeltaZ);
    }
}
