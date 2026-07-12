package cn.aetheris.yuki.check.impl.movement.vehicle.fly;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Collections;
import java.util.OptionalInt;

@CheckData(name = "VehicleFlyB", configName = "VehicleFlyB", type = CheckType.VEHICLE, description = "Abnormal movement around the vehicle", decay = 0.25, setback = 12, experimental = true)
public final class VehicleFlyB extends Check implements PacketCheck {

    private int leavingVehicleTick;

    public VehicleFlyB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final double accel = player.acceleration;
            final double deltaXZ = player.deltaXZ;
            final double deltaY = player.deltaY;
            final double lastDeltaY = player.lastDeltaY;
            final double lastDeltaXZ = player.lastDeltaXZ;
            final boolean shouldCheck = player.uncertaintyHandler.lastHardCollidingLerpingEntity.hasOccurredSince(3);
            final boolean isVehicle = player.inVehicle();
            final boolean serverGround = player.isClientClaimsLastOnGround();

            leavingVehicleTick = isVehicle ? 0 : leavingVehicleTick + 1;

            if (isExempt(ExemptType.TELEPORT,
                    ExemptType.RESPAWN,
                    ExemptType.ELYTRA_FLYING,
                    ExemptType.GSIT_ACTION,
                    ExemptType.INVALID_GAMEMODE,
                    ExemptType.MOVE_LAGGING,
                    ExemptType.LIQUID) || leavingVehicleTick < 5 || exempt()) {
                return;
            }

            if (player.predictedVelocity.isExplosion() || player.predictedVelocity.isKnockback()) {
                return;
            }

            if (player.isFireworkBoost()) {
                return;
            }

            final OptionalInt levitation = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.LEVITATION);
            final OptionalInt speed = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.SPEED);
            final OptionalInt jump = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.JUMP_BOOST);

            if (speed.isPresent() && speed.getAsInt() < 2) {
                return;
            }

            if (levitation.isPresent()) {
                return;
            }

            if (player.getBukkitPlayer() == null) {
                return;
            }

            if (serverGround || isVehicle) {
                
                return;
            }

            if (!shouldCheck) {
                return;
            }

            final PacketLocation location = player.getLocationData();
            if (deltaXZ > 0.425 && deltaXZ == lastDeltaXZ) {
                if (flagAndAlert("(SmoothXZ)\ndx= " + deltaXZ)) {
                    setbackIfAboveSetbackVL();
                }
            }





            if (deltaXZ > 0.63 && deltaY > 0.659) {
                if (flagAndAlert("(ChangeXYZ)\ndy= " + deltaY + "\ndx= " + deltaXZ)) {
                    setbackIfAboveSetbackVL();
                    PaperUtils.teleport(player.bukkitPlayer, location.toLocation(player.bukkitPlayer));
                }
            }
            if (deltaXZ > 0.958) {
                if (flagAndAlert("(ChangeXZ)\ndy= " + deltaY + "\ndx= " + deltaXZ)) {
                    setbackIfAboveSetbackVL();
                    PaperUtils.teleport(player.bukkitPlayer, location.toLocation(player.bukkitPlayer));
                }
            }
            if (deltaY > 0.85 && jump.isPresent() && jump.getAsInt() < 1) {
                if (flagAndAlert("(ChangeY)\ndy= " + deltaY)) {
                    setbackIfAboveSetbackVL();
                    PaperUtils.teleport(player.bukkitPlayer, location.toLocation(player.bukkitPlayer));
                }
            }
            if (deltaY > 0.45 && deltaY == lastDeltaY && jump.isPresent() && jump.getAsInt() < 0) {
                if (flagAndAlert("(SmoothY)\ndy= " + deltaY)) {
                    setbackIfAboveSetbackVL();
                    PaperUtils.teleport(player.bukkitPlayer, location.toLocation(player.bukkitPlayer));
                }
            }
            if (accel > 0.85 && deltaXZ > 0.15 && deltaXZ == lastDeltaXZ) {
                if (flagAndAlert("(SmoothXZ2)\ndx= " + deltaXZ + "\naccel= " + accel)) {
                    setbackIfAboveSetbackVL();
                }
            }
            if (accel < 0.2 && deltaXZ > 0.5 && deltaY > 0.601) {
                if (flagAndAlert("(ChangeXYZ2)\ndx= " + deltaXZ + "\naccel= " + accel + "\ndy= " + deltaY)) {
                    setbackIfAboveSetbackVL();
                }
            }
            if (accel > 6.0 && deltaXZ < 0.85 && deltaXZ != 0) {
                if (flagAndAlert("(ChangeXZ2)\ndx= " + deltaXZ + "\naccel= " + accel)) {
                    setbackIfAboveSetbackVL();
                }
            }
            if (accel > 0.5 && accel < 1.5 && deltaXZ > 0.65 && deltaY > 0.49) {
                if (flagAndAlert("(ChangeXZ3)\ndx= " + deltaXZ + "\ndy= " + deltaY + "\naccel= " + accel)) {
                    setbackIfAboveSetbackVL();
                    PaperUtils.teleport(player.bukkitPlayer, location.toLocation(player.bukkitPlayer));
                }
            }
        }
    }

    private boolean exempt() {
        return Collections.max(player.uncertaintyHandler.pistonX) != 0
                || Collections.max(player.uncertaintyHandler.pistonY) != 0
                || Collections.max(player.uncertaintyHandler.pistonZ) != 0
                || player.uncertaintyHandler.isStepMovement
                || player.isFlying
                || player.canFly
                || player.uncertaintyHandler.isSteppingNearShulker
                || player.uncertaintyHandler.isSteppingOnFence
                || player.compensatedEntities.getSelf().isDead
                || player.isInBed
                || player.lastInBed
                || player.uncertaintyHandler.lastFlyingStatusChange.hasOccurredSince(30);
    }
}