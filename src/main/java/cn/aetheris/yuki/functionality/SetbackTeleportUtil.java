package cn.aetheris.yuki.functionality;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.player.badpackets.packetorder.BadPacketsH;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.packets.patch.ResyncWorldUtil;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngine;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineElytra;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineNormal;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineWater;
import cn.aetheris.yuki.block.chunk.Column;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.movement.SetBackData;
import cn.aetheris.yuki.data.movement.TeleportAcceptData;
import cn.aetheris.yuki.data.movement.TeleportData;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.data.movement.VelocityData;
import cn.aetheris.yuki.data.util.Pair;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.math.VectorUtils;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.GetBoundingBox;
import cn.aetheris.yuki.protocol.nms.ReachUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@CheckData(utilityClass = true)
public final class SetbackTeleportUtil extends Check implements PostPredictionCheck {
    
    public final ConcurrentLinkedQueue<TeleportData> pendingTeleports = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();
    
    
    
    
    public boolean hasAcceptedSpawnTeleport = false;
    
    public boolean blockOffsets = false;
    public SetbackPosWithVector lastKnownGoodPosition;
    
    public boolean isSendingSetback = false;
    public int cheatVehicleInterpolationDelay = 0;
    
    
    @Getter
    private SetBackData requiredSetBack = null;
    private long lastWorldResync = 0;

    public SetbackTeleportUtil(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        
        Vector3dm afterTickFriction = player.clientVelocity.clone();

        
        
        if (predictionComplete.getData().getSetback() != null) {
            
            if (cheatVehicleInterpolationDelay > 0) cheatVehicleInterpolationDelay = 10;
            
            lastKnownGoodPosition = new SetbackPosWithVector(new Vector3d(player.x, player.y, player.z), afterTickFriction);
        } else if (requiredSetBack == null || requiredSetBack.isComplete()) {
            cheatVehicleInterpolationDelay--;
            
            
            lastKnownGoodPosition = new SetbackPosWithVector(new Vector3d(player.x, player.y, player.z), afterTickFriction);
        }

        if (requiredSetBack != null) requiredSetBack.tick();
    }

    public void executeForceResync() {
        if (isExempt()) {
            return; 
        }
        if (lastKnownGoodPosition == null) {
            return; 
        }
        blockMovementsUntilResync(true, true);
        LogUtils.setback("&b " + player.getName() + "&7 has been setback for player should resync &7(&b" + (getCheckName() != null ? getCheckName() : "Nulled" + "&7)"));
    }


    public void executeNonSimulatingSetback() {
        if (isExempt()) {
            return;
        }

        
        if (lastKnownGoodPosition == null) {
            return; 
        }
        blockMovementsUntilResync(false, false);
        LogUtils.setback("&b" + player.getName() + "&7 has been setback for normal setback &7(&b" + (getCheckName() != null ? getCheckName() : "Nulled" + "&7)"));
    }

    public boolean executeViolationSetback() {
        if (isExempt()) {
            return false;
        }
        blockMovementsUntilResync(true, false);
        LogUtils.setback("&b " + player.getName() + "&7 has been setback for violation setback &7(&b" + (getCheckName() != null ? getCheckName() : "Nulled" + "&7)"));
        return true;
    }

    public boolean executeTeleport(PacketLocation needLoc, byte mark, boolean dismountVehicle) {
        if (requiredSetBack == null) return false;
        if (isExempt()) {
            return false;
        }

        ChannelHelper.runInEventLoop(player.getUser().getChannel(), () -> {
            int teleportId = random.nextInt() | Integer.MIN_VALUE;
            PacketEvents.getAPI().getProtocolManager().sendPacketSilently(
                    player.user.getChannel(),
                    new WrapperPlayServerPlayerPositionAndLook(
                            needLoc.getX(),
                            needLoc.getY(),
                            needLoc.getZ(),
                            needLoc.getYaw(),
                            Math.abs(needLoc.getPitch()) > 90 ? 90 : needLoc.getPitch(),
                            mark,
                            teleportId,
                            dismountVehicle));
        });
        LogUtils.setback("&b " + player.getName() + "&7 has been setback for teleport back &7(&b" + (getCheckName() != null ? getCheckName() : "Nulled" + "&7)"));
        return true;
    }

    public boolean executeTeleport(PacketLocation needLoc) {
        if (requiredSetBack == null) return false;
        return executeTeleport(needLoc, requiredSetBack.getTeleportData().getFlags().getMask(), false);
    }

    private boolean isExempt() {
        if (player.isFlying && !PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.flying-check.packet", false)) {
            return true;
        }
        if (player.getTPS() < PluginLoader.INSTANCE.getConfigManager().getConfig().getDoubleElse("function.limit.max-tps", 19)) {
            return true;
        }
        if (PluginLoader.INSTANCE.getLagManager().isLagging(time()) && PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.lag-track.setback", false)) {
            return true;
        }
        if (player.getExemptProcessor().isExempt(ExemptType.INVALID_GAMEMODE)) {
            return true;
        }
        if (isExempt(ExemptType.GSIT_ACTION, ExemptType.BREWERRY_PUSH)) {
            return true;
        }
        
        
        if (lastKnownGoodPosition == null) {
            return true;
        }
        
        if (player.bypass) {
            return true;
        }
        
        return player.bukkitPlayer != null && player.noSetbackPermission;
    }


    private void simulateFriction(Vector3dm vector) {
        
        
        if (player.wasTouchingWater) {
            PredictionEngineWater.staticVectorEndOfTick(player, vector, 0.8F, player.gravity, true);
        } else if (player.wasTouchingLava) {
            vector.multiply(0.5D);
            if (player.hasGravity)
                vector.add(new Vector3dm(0.0D, -player.gravity / 4.0D, 0.0D));
        } else if (player.isGliding) {
            PredictionEngineElytra.getElytraMovement(player, vector, ReachUtils.getLook(player, player.yaw, player.pitch)).multiply(player.stuckSpeedMultiplier).multiply(new Vector3dm(0.99F, 0.98F, 0.99F));
            vector.setY(vector.getY() - 0.05); 
        } else { 
            PredictionEngineNormal.staticVectorEndOfTick(player, vector); 
            vector.multiply(player.stuckSpeedMultiplier); 
        }

        
        new PredictionEngine().applyMovementThreshold(player, new HashSet<>(Collections.singletonList(new VectorData(vector, VectorData.VectorType.BestVelPicked))));
    }

    private void blockMovementsUntilResync(boolean simulateNextTickPosition, boolean isResync) {
        if (requiredSetBack == null) return; 
        if (player.bukkitPlayer != null && player.noSetbackPermission) return; 
        requiredSetBack.setPlugin(false); 
        if (isPendingSetback()) return; 

        
        if (System.currentTimeMillis() - lastWorldResync > 5 * 1000) {
            ResyncWorldUtil.resyncPositions(player, player.boundingBox.copy().expand(1));
            lastWorldResync = System.currentTimeMillis();
        }

        Vector3dm clientVel = lastKnownGoodPosition.vector.clone();

        Pair<VelocityData, Vector3dm> futureKb = player.checkManager.getKnockbackHandler().getFutureKnockback();
        VelocityData futureExplosion = player.checkManager.getExplosionHandler().getFutureExplosion();

        
        if (futureKb.first() != null) {
            clientVel = futureKb.second();
        }

        
        if (futureExplosion != null && (futureKb.first() == null || futureKb.first().transaction < futureExplosion.transaction)) {
            clientVel.add(futureExplosion.vector);
        }

        Vector3d position = lastKnownGoodPosition.pos;

        SimpleCollisionBox oldBB = player.boundingBox;
        player.boundingBox = GetBoundingBox.getPlayerBoundingBox(player, position.getX(), position.getY(), position.getZ());

        
        if (simulateNextTickPosition) {
            Vector3dm collide = Collisions.collide(player, clientVel.getX(), clientVel.getY(), clientVel.getZ());

            position = position.withX(position.getX() + collide.getX());
            position = position.withY(position.getY() + collide.getY());
            
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
                
                
                position = position.withY(position.getY() + SimpleCollisionBox.COLLISION_EPSILON);
            }
            position = position.withZ(position.getZ() + collide.getZ());

            if (clientVel.getX() != collide.getX()) clientVel.setX(0);
            if (clientVel.getY() != collide.getY()) clientVel.setY(0);
            if (clientVel.getZ() != collide.getZ()) clientVel.setZ(0);

            simulateFriction(clientVel);
        }

        player.boundingBox = oldBB; 

        if (!hasAcceptedSpawnTeleport || player.isFlying)
            clientVel = null; 

        
        if (isResync) {
            blockOffsets = true;
        }

        SetBackData data = new SetBackData(new TeleportData(position, new Vector3d(), new RelativeFlag(0b11000), player.lastTransactionSent.get(), 0), player.yaw, player.pitch, clientVel, player.inVehicle(), false);
        sendSetback(data);
    }

    private void sendSetback(SetBackData data) {
        isSendingSetback = true;
        Vector3d position = data.getTeleportData().getLocation();

        try {
            
            if (player.inVehicle()) {
                int vehicleId = player.getRidingVehicleId();
                if (player.compensatedEntities.serverPlayerVehicle != null) {
                    
                    if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
                        HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerSetPassengers(vehicleId, new int[2]));
                    } else {
                        HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerAttachEntity(vehicleId, -1, false));
                    }

                    
                    
                    HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerEntityTeleport(vehicleId, new Vector3d(position.getX(), position.getY(), position.getZ()), player.yaw % 360, 0, false));
                    player.getSetbackTeleportUtil().cheatVehicleInterpolationDelay = Integer.MAX_VALUE; 
                    
                    if (player.bukkitPlayer != null) {
                        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
                            Entity vehicle = player.bukkitPlayer.getVehicle();
                            if (vehicle != null) {
                                vehicle.eject();
                            }
                        });
                    }
                }
            }

            double y = position.getY();
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
                y += 1.62; 
            }

            
            player.sendTransaction();

            
            int teleportId = random.nextInt() | Integer.MIN_VALUE;
            data.setPlugin(false);
            data.getTeleportData().setTeleportId(teleportId);
            data.getTeleportData().setTransaction(player.lastTransactionSent.get());

            
            addSentTeleport(new Location(null, position.getX(), y, position.getZ(), player.yaw % 360, player.pitch % 360), new Vector3d(), data.getTeleportData().getTransaction(), new RelativeFlag(0b11000), false, teleportId);
            
            requiredSetBack = data;
            
            PacketEvents.getAPI().getProtocolManager().sendPacketSilently(player.user.getChannel(), new WrapperPlayServerPlayerPositionAndLook(position.getX(), position.getY(), position.getZ(), 0, 0, data.getTeleportData().getFlags().getMask(), teleportId, false));
            player.sendTransaction();

            if (data.getVelocity() != null && data.getVelocity().lengthSquared() > 0) {
                HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerEntityVelocity(player.entityID, new Vector3d(data.getVelocity().getX(), data.getVelocity().getY(), data.getVelocity().getZ())));
            }
        } finally {
            isSendingSetback = false;
        }
    }

    
    public TeleportAcceptData checkTeleportQueue(double x, double y, double z) {
        
        
        TeleportAcceptData teleportData = new TeleportAcceptData();

        TeleportData teleportPos;
        while ((teleportPos = pendingTeleports.peek()) != null) {
            double trueTeleportX = (teleportPos.isRelativeX() ? player.x : 0) + teleportPos.getLocation().getX();
            double trueTeleportY = (teleportPos.isRelativeY() ? player.y : 0) + teleportPos.getLocation().getY();
            double trueTeleportZ = (teleportPos.isRelativeZ() ? player.z : 0) + teleportPos.getLocation().getZ();

            
            Vector3d clamped = VectorUtils.clampVector(new Vector3d(trueTeleportX, trueTeleportY, trueTeleportZ));
            double threshold = teleportPos.isRelativePos() ? player.getMovementThreshold() : 0;
            boolean closeEnoughY = Math.abs(clamped.getY() - y) <= 1e-7 + threshold; 

            if (player.lastTransactionReceived.get() == teleportPos.getTransaction() && Math.abs(clamped.getX() - x) <= threshold && closeEnoughY && Math.abs(clamped.getZ() - z) <= threshold) {
                pendingTeleports.poll();
                hasAcceptedSpawnTeleport = true;
                blockOffsets = false;

                
                
                if (requiredSetBack != null && requiredSetBack.getTeleportData().getTransaction() == teleportPos.getTransaction()) {
                    teleportData.setSetback(requiredSetBack);
                    requiredSetBack.setComplete(true);
                }

                teleportData.setTeleportData(teleportPos);
                teleportData.setTeleport(true);
                break;
            } else if (player.lastTransactionReceived.get() > teleportPos.getTransaction()) {
                
                if (!player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(3)) {
                    player.checkManager.getCheck(BadPacketsH.class).flagAndAlert("threshold= " + threshold);
                }
                pendingTeleports.poll();
                requiredSetBack.setPlugin(false);
                if (pendingTeleports.isEmpty()) {
                    sendSetback(requiredSetBack);
                }
                continue;
            }
            
            break;
        }

        return teleportData;
    }

    
    public boolean checkVehicleTeleportQueue(double x, double y, double z) {
        int lastTransaction = player.lastTransactionReceived.get();

        while (true) {
            Pair<Integer, Vector3d> teleportPos = player.vehicleData.vehicleTeleports.peek();
            if (teleportPos == null) break;
            if (lastTransaction < teleportPos.first()) {
                break;
            }

            Vector3d position = teleportPos.second();
            if (position.getX() == x && position.getY() == y && position.getZ() == z) {
                player.vehicleData.vehicleTeleports.poll();

                return true;
            } else if (lastTransaction > teleportPos.first() + 1) {
                player.vehicleData.vehicleTeleports.poll();

                
                
                continue;
            }

            break;
        }

        return false;
    }

    
    public boolean shouldBlockMovement() {
        
        
        return insideUnloadedChunk() || blockOffsets || (requiredSetBack != null && !requiredSetBack.isComplete());
    }

    private boolean isPendingSetback() {
        
        if (requiredSetBack.getTeleportData().isRelativeX() || requiredSetBack.getTeleportData().isRelativeY() || requiredSetBack.getTeleportData().isRelativeZ()) {
            return false;
        }
        
        return requiredSetBack != null && !requiredSetBack.isComplete();
    }

    
    public boolean insideUnloadedChunk() {
        Column column = player.compensatedWorld.getChunk(MathUtil.floor(player.x) >> 4, MathUtil.floor(player.z) >> 4);
        if (player.bypass) {
            return false;
        }
        
        return (column == null || column.transaction() >= player.lastTransactionReceived.get() ||
                
                !player.getSetbackTeleportUtil().hasAcceptedSpawnTeleport);
    }

    public void addSentTeleport(Location position, Vector3d velocity, int transaction, RelativeFlag flags, boolean plugin, int teleportId) {
        TeleportData data = new TeleportData(new Vector3d(position.getX(), position.getY(), position.getZ()), velocity, flags, transaction, teleportId);
        pendingTeleports.add(data);

        Vector3d safePosition = new Vector3d(position.getX(), position.getY(), position.getZ());

        
        if (flags.has(RelativeFlag.X)) {
            safePosition = safePosition.withX(safePosition.getX() + lastKnownGoodPosition.pos.getX());
        }

        if (flags.has(RelativeFlag.Y)) {
            safePosition = safePosition.withY(safePosition.getY() + lastKnownGoodPosition.pos.getY());
        }

        if (flags.has(RelativeFlag.Z)) {
            safePosition = safePosition.withZ(safePosition.getZ() + lastKnownGoodPosition.pos.getZ());
        }

        data = new TeleportData(safePosition, velocity, new RelativeFlag(0b11000), transaction, teleportId);
        requiredSetBack = new SetBackData(data, player.yaw, player.pitch, null, false, plugin);

        this.lastKnownGoodPosition = new SetbackPosWithVector(safePosition, new Vector3dm());
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class SetbackPosWithVector {
        private Vector3d pos;
        private Vector3dm vector;
    }
}