package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngine;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.player.KnownInput;
import cn.aetheris.yuki.entity.PacketEntity;
import cn.aetheris.yuki.entity.PacketEntityHorse;
import cn.aetheris.yuki.protocol.nms.vec.Vec2;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;

public final class PacketPlayerSteer extends AbstractPacketListener {

    public PacketPlayerSteer() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            WrapperPlayClientSteerVehicle steer = new WrapperPlayClientSteerVehicle(event);

            float forwards = steer.getForward();
            float sideways = steer.getSideways();

            player.vehicleData.nextVehicleForward = forwards;
            player.vehicleData.nextVehicleHorizontal = sideways;

            this.tickPlayerWorld(player);
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            WrapperPlayClientPlayerInput input = new WrapperPlayClientPlayerInput(event);
            byte forward = 0;
            byte sideways = 0;
            if (input.isForward()) {
                forward++;
            }

            if (input.isBackward()) {
                forward--;
            }

            if (input.isLeft()) {
                sideways++;
            }

            if (input.isRight()) {
                sideways--;
            }

            Vec2 inputVector = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)
                    ? PredictionEngine.modifyInput(player, new Vec2(forward, sideways).normalized())
                    : new Vec2(forward * 0.98f, sideways * 0.98f);

            player.vehicleData.nextVehicleForward = inputVector.x();
            player.vehicleData.nextVehicleHorizontal = inputVector.y();

            
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_6)) {
                player.isSneaking = input.isShift();
            }

            player.packetStateData.knownInput = new KnownInput(input.isForward(), input.isBackward(), input.isLeft(), input.isRight(), input.isJump(), input.isShift(), input.isSprint());
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
            PlayerData player = getData(event.getUser());
            if (player == null || !player.inVehicle() || player.getClientVersion().isOlderThan(ClientVersion.V_1_21_2))
                return;

            
            this.tickPlayerWorld(player);
        }
    }

    private void tickPlayerWorld(PlayerData player) {
        PacketEntity riding = player.compensatedEntities.self.getRiding();

        
        
        
        if (player.packetStateData.receivedSteerVehicle && riding != null) {
            
            
            if ((riding.isBoat || riding.isHappyGhast || (riding instanceof PacketEntityHorse horse && horse.hasSaddle())) &&
                    riding.passengers.get(0) == player.compensatedEntities.self &&
                    
                    player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) &&
                    
                    Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
                return;
            }

            
            player.compensatedWorld.tickPlayerInPistonPushingArea();
            player.compensatedEntities.tick();

            
            player.vehicleData.lastDummy = true;

            
            int controllingEntityId = player.inVehicle() ? player.getRidingVehicleId() : player.entityID;
            player.firstBreadKB = player.checkManager.getKnockbackHandler().calculateFirstBreadKnockBack(controllingEntityId, player.lastTransactionReceived.get());
            player.likelyKB = player.checkManager.getKnockbackHandler().calculateRequiredKB(controllingEntityId, player.lastTransactionReceived.get(), false);

            
            if (player.firstBreadKB != null) {
                player.clientVelocity = player.firstBreadKB.vector;
            }
            if (player.likelyKB != null) {
                player.clientVelocity = player.likelyKB.vector;
            }

            player.firstBreadExplosion = player.checkManager.getExplosionHandler().getFirstBreadAddedExplosion(player.lastTransactionReceived.get());
            player.likelyExplosions = player.checkManager.getExplosionHandler().getPossibleExplosions(player.lastTransactionReceived.get(), false);

            
            player.checkManager.getExplosionHandler().forceExempt();
            player.checkManager.getKnockbackHandler().forceExempt();

            
            
            
            
            player.lastX = player.x;
            player.lastY = player.y;
            player.lastZ = player.z;

            SimpleCollisionBox vehiclePos = player.compensatedEntities.self.getRiding().getPossibleCollisionBoxes();

            player.x = (vehiclePos.minX + vehiclePos.maxX) / 2;
            player.y = (vehiclePos.minY + vehiclePos.maxY) / 2;
            player.z = (vehiclePos.minZ + vehiclePos.maxZ) / 2;

            if (player.isSprinting != player.lastSprinting) {
                player.compensatedEntities.hasSprintingAttributeEnabled = player.isSprinting;
            }
            player.lastSprinting = player.isSprinting;
        }

        player.packetStateData.receivedSteerVehicle = true;
    }

}