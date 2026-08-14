package cn.aetheris.yuki.check.util.handler;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(utilityClass = true)
public final class CancelHandler extends Check implements PacketCheck {

    private final boolean hasPrem;

    private boolean enableSync;
    private boolean enableMovementInteract;
    private boolean enableMovementWrongOffset;
    private boolean enableVehicleMovementWrongOffset;
    private boolean enableVehiclePacket;
    private boolean enableVehicleInBed;
    private boolean enableSyncDied;
    private boolean enableMovementDiedBukkit;
    private boolean enableMovementDiedPacket;

    public CancelHandler(PlayerData playerData) {
        super(playerData);
        hasPrem = playerData.getBukkitPlayer() != null && playerData.getBukkitPlayer().hasPermission("yuki.exempt.cancel");
    }

    public void onPacketReceive(final PacketReceiveEvent event) {
        if (player.bypass || hasPrem || !enableSync) {
            return;
        }

        if (event.isCancelled()) return;

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && enableMovementInteract) {
            if (player.getSetbackTeleportUtil().cheatVehicleInterpolationDelay > 0) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled ENTITY_INTERACT packet for player in invalid vehicle");
            }
        }

        if (player.packetStateData.lastPacketWasTeleport) return;

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (enableMovementWrongOffset && !isExempt(ExemptType.GSIT_ACTION)) {
                if (player.getSetbackTeleportUtil().shouldBlockMovement()) {
                    if (buffer++ > 3) { 
                        event.setCancelled(true);
                        LogUtils.cancel("&b" + player.getName() + "&7 Canceled ALL_PACKET for invalid offset handle (&b" + buffer + "&7)");
                    }
                } else {
                    rewardBufferAndVL();
                }
            }

            if (player.inVehicle() && !isExempt(ExemptType.GSIT_ACTION) && event.getPacketType() != PacketType.Play.Client.PLAYER_ROTATION) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled VEHICLE packet for player in vehicle but not send pos packet");
            }

            if (player.isInBed && new Vector3d(player.x, player.y, player.z).distanceSquared(player.bedPosition) > 1 && PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.in-bed")) {
                event.setCancelled(true);
                player.mitigateDamage();
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled ALL_PACKET for player sent in bed packet but server not");
            }

            boolean bukkitDeath = player.bukkitPlayer != null && player.bukkitPlayer.isDead() && enableMovementDiedBukkit;
            boolean packetDead = player.compensatedEntities.getSelf().isDead && enableMovementDiedPacket;

            if (enableSyncDied && (packetDead || bukkitDeath)) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled ALL_PACKET for player death");
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            if (player.getSetbackTeleportUtil().shouldBlockMovement() && !isExempt(ExemptType.GSIT_ACTION) && enableVehicleMovementWrongOffset) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled VEHICLE_MOVE packet for invalid offset handle");
            }
            if (!player.inVehicle() && enableVehiclePacket) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled VEHICLE packet for player in vehicle but not server not");
            }
            if (player.isInBed && enableVehicleInBed) {
                event.setCancelled(true);
                LogUtils.cancel("&b" + player.getName() + "&7 Canceled VEHICLE for player sent in bed packet but server not");
            }
            handleDeathPackets(event);
        }
    }


    private void handleDeathPackets(PacketReceiveEvent event) {
        boolean bukkitDeath = player.bukkitPlayer != null && player.bukkitPlayer.isDead() && enableMovementDiedBukkit;
        boolean packetDead = player.compensatedEntities.getSelf().isDead && enableMovementDiedPacket;

        if (enableSyncDied && (packetDead || bukkitDeath)) {
            event.setCancelled(true);
            LogUtils.cancel("&b" + player.getName() + "&7 Canceled ALL_PACKET for player death");
        }
    }

    @Override
    public void reload() {
        super.reload();
        this.enableSync = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.enable");
        this.enableMovementInteract = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.interact");
        this.enableMovementWrongOffset = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.wrong-offset.enable");
        this.enableVehicleMovementWrongOffset = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.vehicles.wrong-offset");
        this.enableVehiclePacket = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.vehicles.packet");
        this.enableVehicleInBed = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.vehicles.in-bed");
        this.enableSyncDied = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.died.enable");
        this.enableMovementDiedBukkit = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.died.bukkit");
        this.enableMovementDiedPacket = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("sync.movement.died.packet");
    }
}
