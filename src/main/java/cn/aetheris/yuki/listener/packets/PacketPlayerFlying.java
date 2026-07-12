package cn.aetheris.yuki.listener.packets;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.movement.groundspoof.GroundSpoofA;
import cn.aetheris.yuki.listener.packets.abstracts.AbstractPacketListener;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PacketPlayerFlying extends AbstractPacketListener {

    public PacketPlayerFlying() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            return;
        }

        final PlayerData data = getData(event.getUser());
        if (data == null) return;


        if (data.packetStateData.lastPacketWasTeleport || data.packetStateData.lastPacketWasOnePointSeventeenDuplicate) {
            data.totalMovePacketsSent = 0;
            data.totalFlyingPacketsSent = 0;
            return;
        }

        final long now = System.currentTimeMillis();
        data.lastFlyingDelay = now - data.lastFlying;
        data.lastFlying = now;
        data.lagging = (data.lastFlyingDelay < 10L || data.lastFlyingDelay > 90L)
                && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8);

        if (data.lastFlyingDelay > 110L) {
            data.artemisLastDelayedFlyingPacket = now;
        }

        data.placing = false;
        data.isAttacking = false;
        data.respawn = false;

        data.totalFlyingPacketsSent++;
        data.respawnTick++;
        data.windchargeAttackTick++;
        data.sinceWeaponShootTicks++;
        data.sinceMythicMobTicks++;
        data.sinceBukkitCancelMovementTicks++;
        data.sinceMythicMobItemAttackTicks++;
        data.sinceBreweryPushTicks++;
        data.sinceGSitActionTick++;
        data.elytraTicks++;
        data.sinceRiptideSpinTick++;
        data.fireworkBoostTicks++;
        data.outRidingTicks = data.inVehicle() ? 0 : data.outRidingTicks + 1;
        data.sinceChangeGamemodeTick = (data.gamemode != GameMode.SURVIVAL && data.gamemode != GameMode.ADVENTURE) ? 0 : data.sinceChangeGamemodeTick + 1;
        data.vehicleTicks = data.compensatedEntities.getSelf().getRiding() != null ? data.vehicleTicks + 1 : 0;
        if (data.fireworkBoost) data.fireworkBoostTicks = 0;
        if (data.isGliding()) data.elytraTicks = 0;
        if (data.isRiptidePose()) data.sinceRiptideSpinTick = 0;

        final WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        if (wrapper.hasRotationChanged() || wrapper.hasPositionChanged()) {
            final PacketLocation newLoc = new PacketLocation(
                    wrapper.getLocation().getX(),
                    wrapper.getLocation().getY(),
                    wrapper.getLocation().getZ(),
                    wrapper.getLocation().getYaw(),
                    wrapper.getLocation().getPitch(),
                    event.getTimestamp(),
                    data.getBukkitWorldName(),
                    data.onGround
            );

            if (wrapper.hasPositionChanged()) {
                updateMovementData(data, newLoc);
                if ((data.isClimbing || data.isWasTouchingWater()) && data.getDeltaY() > 0.005 && data.isOnGround()) {
                    final GroundSpoofA check = data.getCheckManager().getCheck(GroundSpoofA.class);
                    if (check != null) {
                        check.flipPlayerGroundStatus = true;
                        if (!data.isClientClaimsLastOnGround()) { 
                            check.flagAndAlert("ground= " + wrapper.isOnGround() + " | " + data.isOnGround());
                        }
                    }
                }
                if (now - data.lastLocationData.getTimeStamp() > 110L) {
                    data.lastDelayedMovePacket = now;
                }
                if (PluginLoader.INSTANCE.getConfigManager().isMitigateNoSlowInventory() && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
                    if (data.bukkitPlayer != null) {
                        if (data.isDropItem()) {
                            NMSUtils.resetItemUsage(data.bukkitPlayer);
                        }
                    }
                }
            } else {
                data.totalMovePacketsSent = 0;
            }
        }

        final double maxTPS = PluginLoader.INSTANCE.getConfigManager().getConfig().getDoubleElse("function.limit.max-tps", 19);
        if (data.getTPS() < maxTPS) return;

        final Player bukkitPlayer = data.getBukkitPlayer();
        if (bukkitPlayer == null) return;

        final PacketLocation from = data.getLastLocationData();
        final PacketLocation to = data.getLocationData();

        if (PluginLoader.INSTANCE.getConfigManager().isMitigatePosTeleport()
                && data.gamemode == GameMode.SURVIVAL
                && !data.packetStateData.lastPacketWasTeleport
                && !data.isRespawn()
                && !data.inVehicle()
                && !data.isBypass()
                && !data.isRiptidePose()
                && !data.getSetbackTeleportUtil().insideUnloadedChunk()) {

            double diff = MathUtil.calculateDistance(from, to);
            final double threshold = PluginLoader.INSTANCE.getConfigManager()
                    .getConfig().getDoubleElse("mitigates.invalid-pos.teleport.need", 12);

            if (data.getLocationData() == to) {
                diff = 0;
            }

            if (data.getOutRidingTicks() < 20) {
                diff = 0;
            }

            if (diff >= threshold) {
                final Location corrected = to.toLocation(bukkitPlayer);
                PaperUtils.teleport(bukkitPlayer, corrected);
                LogUtils.mitigate("&b" + data.getName() + "&7 was teleport for player move very quickly (&b" + diff + "&7)");
            }
        }
    }

    private void updateMovementData(PlayerData data, PacketLocation newLoc) {
        data.totalMovePacketsSent++;

        data.lastLocationData = data.locationData;
        data.locationData = newLoc;

        data.lastLastPosition = data.lastPosition;
        data.lastPosition = data.position;
        data.position = true;

        data.lastDeltaX = data.deltaX;
        data.lastDeltaY = data.deltaY;
        data.lastDeltaZ = data.deltaZ;
        data.deltaX = Math.abs(newLoc.getX() - data.lastLocationData.getX());
        data.deltaY = Math.abs(newLoc.getY() - data.lastLocationData.getY());
        data.deltaZ = Math.abs(newLoc.getZ() - data.lastLocationData.getZ());
        data.lastDeltaXZ = data.deltaXZ;
        data.deltaXZ = Math.hypot(data.deltaX, data.deltaZ);

        data.lastAcceleration = data.acceleration;
        data.acceleration = Math.abs(data.deltaXZ - data.lastDeltaXZ);

        data.moving = (data.deltaXZ > 0.0) || (data.deltaY > 0.0);
        data.moveTick = data.moving ? data.moveTick + 1 : 0;
        data.setStandTicks(data.moving ? 0 : data.standTicks + 1);

        data.fireworkBoost = false;
        data.inWeb = false;
    }
}
