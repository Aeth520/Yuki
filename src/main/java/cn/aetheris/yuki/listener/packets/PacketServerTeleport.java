package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.data.RotationData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;
import org.bukkit.Location;

public final class PacketServerTeleport extends AbstractPacketListener {

    private static final boolean STUPID_TELEPORT_SYSTEM = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);

    public PacketServerTeleport() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook teleport = new WrapperPlayServerPlayerPositionAndLook(event);

            PlayerData player = getData(event.getUser());

            Vector3d pos = new Vector3d(teleport.getX(), teleport.getY(), teleport.getZ());

            if (player == null) return;

            
            if (player.getSetbackTeleportUtil().getRequiredSetBack() == null) {
                
                player.x = teleport.getX();
                player.y = teleport.getY();
                player.z = teleport.getZ();
                player.yaw = teleport.getYaw();
                player.pitch = teleport.getPitch();

                player.lastX = teleport.getX();
                player.lastY = teleport.getY();
                player.lastZ = teleport.getZ();
                player.lastYaw = teleport.getYaw();
                player.lastPitch = teleport.getPitch();

                player.pollData();
            }

            
            
            
            
            
            
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) || player.inVehicle()) {
                boolean relativeX = teleport.isRelativeFlag(RelativeFlag.X),
                        relativeY = teleport.isRelativeFlag(RelativeFlag.Y),
                        relativeZ = teleport.isRelativeFlag(RelativeFlag.Z);

                if (relativeX) {
                    pos = pos.add(new Vector3d(player.x, 0, 0));
                    teleport.setRelative(RelativeFlag.X, false);
                }

                if (relativeY) {
                    pos = pos.add(new Vector3d(0, player.y, 0));
                    teleport.setRelative(RelativeFlag.Y, false);
                }

                if (relativeZ) {
                    pos = pos.add(new Vector3d(0, 0, player.z));
                    teleport.setRelative(RelativeFlag.Z, false);
                }

                if (relativeX || relativeY || relativeZ) {
                    teleport.setX(pos.getX());
                    teleport.setY(pos.getY());
                    teleport.setZ(pos.getZ());

                    event.markForReEncode(true);
                }
            }

            if (STUPID_TELEPORT_SYSTEM && player.inVehicle()) {
                boolean relativeDeltaX = teleport.isRelativeFlag(RelativeFlag.DELTA_X),
                        relativeDeltaY = teleport.isRelativeFlag(RelativeFlag.DELTA_Y),
                        relativeDeltaZ = teleport.isRelativeFlag(RelativeFlag.DELTA_Z);

                if (relativeDeltaX) {
                    teleport.setRelative(RelativeFlag.DELTA_X, false);
                }

                if (relativeDeltaY) {
                    teleport.setRelative(RelativeFlag.DELTA_Y, false);
                }

                if (relativeDeltaZ) {
                    teleport.setRelative(RelativeFlag.DELTA_Z, false);
                }

                if (relativeDeltaX || relativeDeltaY || relativeDeltaZ) {
                    teleport.setDeltaMovement(Vector3d.zero());
                    event.markForReEncode(true);
                }
            }

            player.sendTransaction();
            final int lastTransactionSent = player.lastTransactionSent.get();
            event.getTasksAfterSend().add(player::sendTransaction);

            if (teleport.isDismountVehicle()) {
                
                event.getTasksAfterSend().add(() -> player.compensatedEntities.self.eject());
            }

            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_8))
                pos = pos.withY(pos.getY() - 1.62);

            Location target = new Location(null, pos.getX(), pos.getY(), pos.getZ());
            player.getSetbackTeleportUtil().addSentTeleport(target, teleport.getDeltaMovement(), lastTransactionSent, teleport.getRelativeFlags(), true, teleport.getTeleportId());
        }

        if (event.getPacketType() == PacketType.Play.Server.PLAYER_ROTATION) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            WrapperPlayServerPlayerRotation packet = new WrapperPlayServerPlayerRotation(event);

            
            if (!Float.isFinite(packet.getPitch())) {
                packet.setPitch(0);
                event.markForReEncode(true);
            }
            if (Math.abs(packet.getPitch()) > 90) {
                packet.setPitch(0);
                event.markForReEncode(true);
            }
            if (!Float.isFinite(packet.getYaw())) {
                packet.setYaw(0);
                event.markForReEncode(true);
            }

            player.sendTransaction();
            player.pendingRotations.add(new RotationData(packet.getYaw(), MathUtil.clamp(packet.getPitch() % 360F, -90F, 90F), player.getLastTransactionSent()));
            event.getTasksAfterSend().add(player::sendTransaction);
        }

        if (event.getPacketType() == PacketType.Play.Server.VEHICLE_MOVE) {
            WrapperPlayServerVehicleMove vehicleMove = new WrapperPlayServerVehicleMove(event);

            PlayerData player = getData(event.getUser());
            if (player == null) return;

            
            player.setVehicleX(vehicleMove.getPosition().getX());
            player.setVehicleY(vehicleMove.getPosition().getY());
            player.setVehicleZ(vehicleMove.getPosition().getZ());

            player.sendTransaction();
            int lastTransactionSent = player.lastTransactionSent.get();
            Vector3d finalPos = vehicleMove.getPosition();

            event.getTasksAfterSend().add(player::sendTransaction);
            player.vehicleData.vehicleTeleports.add(new Pair<>(lastTransactionSent, finalPos));
        }
    }
}