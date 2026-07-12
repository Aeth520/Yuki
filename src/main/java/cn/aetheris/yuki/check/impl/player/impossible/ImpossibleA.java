package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;

@CheckData(name = "ImpossibleA (Pitch)", configName = "ImpossibleA", description = "Invalid Pitch", type = CheckType.IMPOSSIBLE)
public final class ImpossibleA extends Check implements PacketCheck {

    private boolean teleport;
    private int sinceTeleportTick;
    private int leavingVehicleTick;

    public ImpossibleA(PlayerData player) {
        super(player);
        teleport = false;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isTransaction(event.getPacketType())) {
            leavingVehicleTick = player.inVehicle() ? 0 : leavingVehicleTick + 1;
        }

        if (!isFlying(event.getPacketType())) {
            return;
        }

        if (player.isTeleporting() || player.isRespawn() || player.isWorldChange()) {
            sinceTeleportTick = 0;
        }

        WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

        if (!flying.hasPositionChanged()) {
            return;
        }

        sinceTeleportTick++;

        if (sinceTeleportTick <= 10 || player.getSetbackTeleportUtil().shouldBlockMovement()) {
            teleport = false;
            return;
        }

        final float pitch = player.pitch;
        float maxPitch;
        if (leavingVehicleTick <= 5) {
            maxPitch = 110.5F; 
        } else if (player.isClimbing) {
            maxPitch = 91.2F;
        } else {
            maxPitch = 90F;
        }

        if (pitch > maxPitch || pitch < -90F) {
            if (buffer++ > 2) {
                if (flagAndAlert("p= " + pitch + "\nt= " + sinceTeleportTick + "\nl= " + maxPitch)) {
                    player.pitch = Math.max(Math.min(player.pitch, 90F), -90F);
                    HookInit.getPacketEventsHook().sendPacket(player.getUser(),
                            new WrapperPlayClientPlayerPositionAndRotation(
                                    player.getLocationData().toVector3d(),
                                    player.getYaw(),
                                    player.getPitch(), 
                                    player.isOnGround()
                            )
                    );
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport tp = new WrapperPlayServerEntityTeleport(event);
            if (shouldModifyPackets()) {
                final float pitch = tp.getPitch();
                if (pitch > 90F || pitch < -90F) {
                    int vehicleId = player.compensatedEntities.getPacketEntityID(player.compensatedEntities.getSelf().getRiding());
                    HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerEntityTeleport(vehicleId, tp.getPosition(), player.yaw % 360, 0, false));
                    buffer = 0;
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook playerPositionAndLook = new WrapperPlayServerPlayerPositionAndLook(event);
            teleport = true;
            sinceTeleportTick = 0;
            if (shouldModifyPackets()) {
                final float pitch = playerPositionAndLook.getPitch();
                if (pitch > 90F || pitch < -90F) {
                    HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayClientPlayerPositionAndRotation(playerPositionAndLook.getPosition(), player.yaw % 360, 0, false));
                    buffer = 0;
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.VEHICLE_MOVE) {
            WrapperPlayServerVehicleMove vehicleMove = new WrapperPlayServerVehicleMove(event);
            teleport = true;
            sinceTeleportTick = 0;
            final float pitch = vehicleMove.getPitch();
            if (shouldModifyPackets()) {
                if (pitch > 90F || pitch < -90F) {
                    HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayClientPlayerPositionAndRotation(vehicleMove.getPosition(), player.yaw % 360, 0, false));
                    buffer = 0;
                }
            }
        }
    }
}