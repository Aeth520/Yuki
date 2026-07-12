package cn.aetheris.yuki.check.util.exempts.types;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.time.TimeUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import lombok.Getter;

import java.util.function.Function;

import static com.github.retrooper.packetevents.protocol.player.ClientVersion.UNKNOWN;
import static com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_9;

@Getter
public enum ExemptType {
    JOIN(player -> !TimeUtils.hasExpired(player.joinTime, 10)),
    TELEPORT(player -> player.packetStateData.lastPacketWasTeleport || System.currentTimeMillis() - player.joinTime < 5000L),
    CLIENT_VERSION(player -> player.getClientVersion() == UNKNOWN || player.getClientVersion().isNewerThanOrEquals(V_1_9)),
    CLIENT_ANTICHEAT(PlayerData::isClientACUser),

    BED(player -> player.isInBed || player.lastInBed),
    RIPTIDE(player -> player.getSinceRiptideSpinTick() < 60L),
    BREWERRY_PUSH(player -> player.getSinceBreweryPushTicks() < 12),
    GSIT_ACTION(player -> player.getSinceGSitActionTick() < 35),
    DIED(player -> player.compensatedEntities.getSelf().isDead),
    RESPAWN(player -> player.getRespawnTick() < 5),
    FLYING(player -> player.isFlying || player.canFly),
    ELYTRA_FLYING(player -> player.isGliding),
    INVALID_GAMEMODE(player -> player.getGamemode() != GameMode.SURVIVAL && player.getGamemode() != GameMode.ADVENTURE),
    LOW_FOOD(player -> player.food < 6.0F),
    LIQUID(player -> player.wasTouchingWater || player.wasTouchingLava),
    SEEM_WATER(player -> player.wasEyeInWater),
    SLIGHTLY_TOUCHING_LIQUID(player -> player.slightlyTouchingLava || player.slightlyTouchingWater),
    SWIMMING(player -> player.isSwimming),
    WAS_SWIMMING(player -> player.wasSwimming),
    CLIMBING(player -> player.isClimbing),
    IN_AIR(player -> !player.onGround || !player.lastOnGround || !player.clientClaimsLastOnGround),
    SPRINTING(player -> player.isSprinting),
    MOVING(player -> player.moving),
    PLACING(player -> player.placing),
    ITEM(player -> player.packetStateData.isSlowedByUsingItem()),

    COMBAT(player -> player.hasAttackedSince(2000L)),
    NOT_COMBAT(player -> TimeUtils.hasExpired(player.lastAttack, 3)),
    MYTHIC_ITEM_ATTACK(player -> player.getSinceMythicMobItemAttackTicks() < 20),
    MYTHIC_MOB(player -> player.getSinceMythicMobTicks() < 50),

    NEXT_FARMLAND(player -> player.uncertaintyHandler.isSteppingOnFarmland),
    NEXT_FENCE(player -> player.uncertaintyHandler.isSteppingOnFence),
    NEXT_HONEY(player -> player.uncertaintyHandler.isSteppingOnHoney),
    NEXT_ICE(player -> player.uncertaintyHandler.isSteppingOnIce),
    NEXT_SLIME(player -> player.uncertaintyHandler.isSteppingOnSlime),

    INTERACT(player -> player.digging
            || player.basicDigging
            || player.finishDigging
            || player.dropItem
            || player.isPlacing()
            || player.hasBlockPlaceSince(250L)
            || player.packetStateData.isSlowedByUsingItem()),

    HIGH_C00(player -> player.getKeepAlivePing() > 250),
    HIGH_C0F(player -> player.getTransactionPing() > 160),
    LAGGING(PlayerData::isFlyingLagging),
    MOVE_LAGGING(PlayerData::isMoveLagging),
    INVALID_MOVEMENT(player -> player.bukkitPlayer != null && player.bukkitPlayer.getLocation().distanceSquared(player.bukkitPlayer.getLocation()) < 9.0E-4D),

    SERVER_VERSION(player -> Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)),
    NFPGAY(player -> Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9) && Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_15_2)),

    VEHICLE(PlayerData::inVehicle),
    VEHICLE_SWITCH(player -> player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(3)),
    VEHICLE_DIED(player -> player.inVehicle() && player.compensatedEntities.getSelf().getRiding().isDead),

    SERVER_SENT_PULLBACK(player -> player.getSetbackTeleportUtil().isSendingSetback),
    SERVER_SENT_ROTATE(player -> player.isSentRotate),

    VOID(player -> Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_18) ? (player.y < -70.0) : (player.y < 0.0) || player.isVoid()),

    WEB(PlayerData::isInWeb),
    WEAPON_SHOOT(player -> player.getSinceWeaponShootTicks() < 5);

    private final Function<PlayerData, Boolean> exception;

    ExemptType(final Function<PlayerData, Boolean> exception) {
        this.exception = exception;
    }
}
