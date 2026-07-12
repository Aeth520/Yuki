package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;


@CheckData(name = "InventoryG (Moving)",
        configName = "InventoryG",
        description = "Clicking inventory while moving",
        type = CheckType.INVENTORY,
        decay = 0.7,
        setback = 8)
public final class InventoryG extends InventoryCheck {

    @Getter
    private final EvictingQueue<Velocity> velocityData;
    private long lastFlag;

    public InventoryG(PlayerData player) {
        super(player);
        velocityData = new EvictingQueue<>(100);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            if (player.packetStateData.lastPacketWasOnePointSeventeenDuplicate ||
                    getHorizontalVelocity() > 0 ||
                    getVerticalVelocity() > 0 ||
                    player.getPredictedVelocity().isExplosion() ||
                    player.isRiptidePose) {
                return;
            }

            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)
                    && (player.isHorizontalCollision() || player.isVerticalCollision())) {
                return;
            }


            if (isExempt(ExemptType.SWIMMING, ExemptType.LIQUID, ExemptType.VEHICLE,
                    ExemptType.FLYING, ExemptType.TELEPORT, ExemptType.ELYTRA_FLYING)) {
                return;
            }

            if (player.getUncertaintyHandler().isSteppingOnIce
                    || player.getUncertaintyHandler().isSteppingOnSlime) {
                return;
            }

            if (player.exemptOnGround()) {
                return;
            }

            if (player.getSetbackTeleportUtil().shouldBlockMovement()) {
                return;
            }

            double deltaXZ = player.getDeltaXZ();
            double lastDeltaXZ = player.getLastDeltaXZ();
            double accel = deltaXZ - lastDeltaXZ;

            if (deltaXZ > 0.21D && accel >= 0D) {
                if (time() - lastFlag < 1000L) {
                    return;
                }
                if (shouldModifyPackets() && flagAndAlert("dx= " + deltaXZ + "\naccel= " + accel)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    lastFlag = time();
                    setbackIfAboveSetbackVL();
                    closeInventory();
                    player.getInventory().requiresRefresh = true;
                }
            } else {
                rewardVL();
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            velocityData.removeIf(Velocity::isCompleted);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            handleEntityVelocity(event);
        } else if (event.getPacketType() == PacketType.Play.Server.EXPLOSION) {
            handleExplosion(event);
        }
    }

    private void handleEntityVelocity(PacketSendEvent event) {
        WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
        if (wrapper.getEntityId() != player.entityID) {
            return;
        }
        double x = wrapper.getVelocity().getX() / 8000d;
        double y = wrapper.getVelocity().getY() / 8000d;
        double z = wrapper.getVelocity().getZ() / 8000d;
        short transaction = (short) (player.lastTransactionSent.get() + 1);
        player.sendTransaction();
        velocityData.add(new Velocity(transaction, x, y, z));
    }

    private void handleExplosion(PacketSendEvent event) {
        WrapperPlayServerExplosion wrapper = new WrapperPlayServerExplosion(event);

        final @Nullable WrapperPlayServerExplosion.BlockInteraction blockInteraction = wrapper.getBlockInteraction();
        final boolean shouldDestroy = blockInteraction != WrapperPlayServerExplosion.BlockInteraction.KEEP_BLOCKS;
        if ((wrapper.getRecords() != null && wrapper.getRecords().isEmpty()) || !shouldDestroy) {
            return;
        }

        double x;
        double y;
        double z;

        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            final Vector3d explosion = wrapper.getKnockback();
            if (explosion == null) {
                return;
            }
            x = explosion.getX();
            y = explosion.getY();
            z = explosion.getZ();
        } else {
            final Vector3f explosion = wrapper.getPlayerMotion();
            if (explosion == null) {
                return;
            }
            x = explosion.getX();
            y = explosion.getY();
            z = explosion.getZ();
        }

        if (x == 0.0D && y == 0.0D && z == 0.0D) {
            return;
        }
        short transaction = (short) (player.lastTransactionSent.get() + 1);
        player.sendTransaction();
        velocityData.add(new Velocity(transaction, x, y, z));
    }

    public double getHorizontalVelocity() {
        if (velocityData.isEmpty()) {
            return 0;
        }
        double velocitySum = 0;
        for (Velocity velocity : velocityData) {
            velocitySum += velocity.getHorizontalVelocity();
        }
        return velocitySum;
    }


    public double getVerticalVelocity() {
        if (velocityData.isEmpty()) {
            return 0;
        }
        double velocitySum = 0;
        for (Velocity velocity : velocityData) {
            velocitySum += velocity.getVerticalVelocity();
        }
        return velocitySum;
    }


    public double getVelocityX() {
        if (velocityData.isEmpty()) {
            return 0;
        }
        for (Velocity velocity : velocityData) {
            return velocity.getVelocityX();
        }
        return 0;
    }


    public double getVelocityZ() {
        if (velocityData.isEmpty()) {
            return 0;
        }
        for (Velocity velocity : velocityData) {
            return velocity.getVelocityZ();
        }
        return 0;
    }


    @Getter
    @Setter
    public final class Velocity {

        private final double horizontalVelocity;

        private final double verticalVelocity;

        @Getter
        private final double velocityX;

        @Getter
        private final double velocityZ;

        private final short transaction;

        private final int completedTick;

        
        public Velocity(short transaction, double velocityX, double verticalVelocity, double velocityZ) {
            this.velocityX = velocityX;
            this.velocityZ = velocityZ;
            this.horizontalVelocity = Math.hypot(velocityX, velocityZ);
            this.verticalVelocity = verticalVelocity;
            this.transaction = transaction;
            this.completedTick = calculateCompletedTick();
        }

        private int calculateCompletedTick() {
            int ticks = player.totalFlyingPacketsSent;
            return (int) (ticks + ((horizontalVelocity / 2 + 2) * 15));
        }

        public boolean isCompleted() {
            return player.totalFlyingPacketsSent > completedTick;
        }
    }
}
