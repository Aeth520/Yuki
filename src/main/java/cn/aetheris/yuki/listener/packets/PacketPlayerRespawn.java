package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.movement.elytra.ElytraC;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.KnownInput;
import cn.aetheris.yuki.data.TrackerData;
import cn.aetheris.yuki.entity.PacketEntitySelf;
import cn.aetheris.yuki.util.enums.Pose;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

import java.util.List;
import java.util.Objects;



public class PacketPlayerRespawn extends AbstractPacketListener {

    private static final byte KEEP_ATTRIBUTES = 1;
    private static final byte KEEP_TRACKED_DATA = 2;
    private static final byte KEEP_ALL = 3;

    public PacketPlayerRespawn() {
        super(PacketListenerPriority.HIGH);
    }

    private boolean hasFlag(WrapperPlayServerRespawn respawn, byte flag) {
        
        if (flag == KEEP_ATTRIBUTES) {
            
            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_15)) {
                return false;
            }
        } else if (flag == KEEP_TRACKED_DATA) {
            
            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_15)) {
                return true;
            }
        }

        return (respawn.getKeptData() & flag) != 0;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth health = new WrapperPlayServerUpdateHealth(event);

            PlayerData player = getData(event.getUser());
            if (player == null) return;
            if (player.packetStateData.lastFood == health.getFood()
                    && player.packetStateData.lastHealth == health.getHealth()
                    && player.packetStateData.lastSaturation == health.getFoodSaturation()
                    && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9))
                return;

            player.packetStateData.lastFood = health.getFood();
            player.packetStateData.lastHealth = health.getHealth();
            player.packetStateData.lastSaturation = health.getFoodSaturation();

            player.sendTransaction();

            if (health.getFood() == 20) { 
                player.latencyUtils.addRealTimeTask(player.lastTransactionReceived.get(), () -> player.food = 20);
            } else { 
                player.latencyUtils.addRealTimeTask(player.lastTransactionReceived.get() + 1, () -> player.food = health.getFood());
            }

            if (health.getHealth() <= 0) {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.compensatedEntities.self.isDead = true);
            } else {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> player.compensatedEntities.self.isDead = false);
            }

            event.getTasksAfterSend().add(player::sendTransaction);
        }

        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            PlayerData player = getData(event.getUser());
            if (player == null) return;

            WrapperPlayServerJoinGame joinGame = new WrapperPlayServerJoinGame(event);
            player.gamemode = joinGame.getGameMode();
            player.entityID = joinGame.getEntityId();
            player.dimensionType = joinGame.getDimensionType();
            player.worldName = joinGame.getWorldName();

            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_17))
                return;
            player.compensatedWorld.setDimension(joinGame.getDimensionType());
        }

        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn(event);

            PlayerData player = getData(event.getUser());
            if (player == null) return;

            List<Runnable> tasks = event.getTasksAfterSend();
            tasks.add(player::sendTransaction);

            
            
            
            player.getSetbackTeleportUtil().hasAcceptedSpawnTeleport = false;
            player.getSetbackTeleportUtil().lastKnownGoodPosition = null;

            
            if (isWorldChange(player, respawn)) {
                player.compensatedEntities.serverPositionsMap.clear();
            }

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> {
                
                if (player.getClientVersion().isOlderThan(ClientVersion.V_1_16) || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20)) {
                    player.isSneaking = false;
                }

                player.lastOnGround = false;
                player.clientClaimsLastOnGround = false;
                player.onGround = false;
                player.isInBed = false;
                player.packetStateData.setSlowedByUsingItem(false);
                player.packetStateData.packetPlayerOnGround = false; 
                player.packetStateData.lastClaimedPosition = new Vector3d();
                player.filterMojangStupidityOnMojangStupidity = new Vector3d();
                player.isAttacking = false;
                player.respawn = true;
                player.worldChange = true;

                player.respawnTick = 0;
                final boolean keepTrackedData = this.hasFlag(respawn, KEEP_TRACKED_DATA);

                if (!keepTrackedData) {
                    player.powderSnowFrozenTicks = 0;
                    player.compensatedEntities.self.hasGravity = true;
                    player.playerEntityHasGravity = true;
                    player.packetStateData.knownInput = new KnownInput(false, false, false, false, false, false, false);
                    player.checkManager.getCheck(ElytraC.class).exempt = true;

                    
                    if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_4)) {
                        player.isSprinting = false;
                    } else {
                        player.lastSprintingForSpeed = false;
                    }
                }

                
                if (isWorldChange(player, respawn)) {
                    player.compensatedEntities.entityMap.clear();
                    player.compensatedWorld.activePistons.clear();
                    player.compensatedWorld.openShulkerBoxes.clear();
                    player.compensatedWorld.chunks.clear();
                    player.compensatedWorld.isRaining = false;
                }
                player.dimensionType = respawn.getDimensionType();
                player.worldName = respawn.getWorldName().orElse(null);

                player.compensatedEntities.serverPlayerVehicle = null; 
                player.compensatedEntities.self = new PacketEntitySelf(player, player.compensatedEntities.self);
                player.compensatedEntities.selfTrackedEntity = new TrackerData(0, 0, 0, 0, 0, EntityTypes.PLAYER, player.lastTransactionSent.get());

                if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14)) { 
                    player.isSprinting = false;
                    
                    
                    player.compensatedEntities.hasSprintingAttributeEnabled = false;
                }
                player.pose = Pose.STANDING;
                player.clientVelocity = new Vector3dm();
                player.gamemode = respawn.getGameMode();
                if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
                    player.compensatedWorld.setDimension(respawn.getDimensionType());
                }

                if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16) && !this.hasFlag(respawn, KEEP_ATTRIBUTES)) {
                    
                    player.compensatedEntities.self.resetAttributes();
                    player.compensatedEntities.hasSprintingAttributeEnabled = false;
                }
            });
        }
    }

    private boolean isWorldChange(PlayerData player, WrapperPlayServerRespawn respawn) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16) && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_16)) {
            return !Objects.equals(respawn.getWorldName().orElse(null), player.worldName);
        }

        ClientVersion version = Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion();
        return respawn.getDimensionType().getId(version) != player.dimensionType.getId(version)
                || !Objects.equals(respawn.getDimensionType().getName(), player.dimensionType.getName());
    }
}