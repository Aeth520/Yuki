package cn.aetheris.yuki.check.impl.player.blink;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.location.PacketLocation;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;

@CheckData(
        name = "BlinkA (Sync)",
        configName = "BlinkA",
        description = "Advanced blink detection using window/ping packets",
        type = CheckType.BLINK,
        decay = 1.2,
        experimental = true
)
public final class BlinkA extends Check implements PacketCheck {

    private int chatBuffer;
    private int moveBuffer;
    private int trackingBuffer;
    private int move2Buffer;
    private long lastChatFlag;
    private long lastMoveFlag;
    private long lastTrackingFlag;
    private long lastMove2Flag;
    private long blinkTime = -1;
    private long lastBlinkTime = -1;
    private PacketLocation loc;

    private boolean needClean;

    
    private boolean shouldSetBackToVoid;
    private boolean shouldTeleportBack;
    private boolean shouldCancelPacket;
    private int cancelThreshold;
    private int maxBlinkTime;
    private int maxChatBuffer;
    private int maxMoveBuffer;
    private int maxTrackingBuffer;
    private int maxMove2Buffer;

    public BlinkA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isTransaction(event.getPacketType())) {
            updateBlinkTimestamps();
            Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), () -> loc = player.getLocationData(), 5L);
            return;
        }

        final long currentTime = time();
        final int timeDiff = (int) (currentTime - lastBlinkTime);

        if (timeDiff > cancelThreshold && shouldCancelPacket && shouldModifyPackets()) event.setCancelled(true);

        handleChatDetection(event, currentTime, timeDiff);
        handleMovementDetection(event, currentTime, timeDiff);
        handleEntityAttackDetection(event, currentTime, timeDiff);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            handleWindowConfirmation(event);
            handlePingDetection(event);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.PING) {
            handlePingDetection(event);
        }
    }

    private void updateBlinkTimestamps() {
        lastBlinkTime = blinkTime;
        blinkTime = time();
    }

    private void handleChatDetection(PacketReceiveEvent event, long currentTime, int timeDiff) {
        if (!isChatPacket(event.getPacketType())) return;

        if (timeDiff > 500 && checkCooldown(currentTime, lastChatFlag, 1000)) {
            if (++chatBuffer > maxChatBuffer) {
                if (flagAndAlert("(CHAT) d=" + timeDiff)) {
                    lastChatFlag = currentTime;
                    chatBuffer = 0;
                }
            }
        } else {
            chatBuffer = Math.max(0, chatBuffer - 1);
        }
    }

    private void handleMovementDetection(PacketReceiveEvent event, long currentTime, int timeDiff) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;

        player.setVoid(!player.isOnGround()
                && !player.isLastOnGround()
                && onVoid(player.lastLocationData));

        if (timeDiff > maxBlinkTime && checkCooldown(currentTime, lastMoveFlag, 800)) {
            if (++moveBuffer > maxMoveBuffer) {
                if (flagAndAlert("(MOVE)\nd= " + timeDiff)) {
                    player.mitigateDamage();
                    lastMoveFlag = currentTime;
                    moveBuffer = 0;
                }
                player.sendTransaction();
            }
        } else {
            moveBuffer = Math.max(0, moveBuffer - 1);
        }
    }

    private void handleEntityAttackDetection(PacketReceiveEvent event, long currentTime, int timeDiff) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

        if (timeDiff > 500 && checkCooldown(currentTime, lastTrackingFlag, 800)) {
            if (++trackingBuffer > maxTrackingBuffer) {
                if (flagAndAlert("(TRACKING)\nd= " + timeDiff)) {
                    player.mitigateDamage();
                    lastTrackingFlag = currentTime;
                    trackingBuffer = 0;
                }
                player.sendTransaction();
            }
        } else {
            trackingBuffer = Math.max(0, trackingBuffer - 1);
        }
    }

    private void handleWindowConfirmation(PacketSendEvent event) {
        WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
        if (confirmation.isAccepted()) {
            updateBlinkTimestamps();
            loc = player.getLocationData();
        }
    }

    private void handlePingDetection(PacketSendEvent event) {
        if (lastBlinkTime <= 0 || loc == null) return;


        if (isExempt(ExemptType.JOIN)) {
            updateBlinkTimestamps();
            return;
        }

        if (player.getCompensatedEntities().getSelf().isDead) {
            rewardVL();
            updateBlinkTimestamps();
            return;
        }

        if (player.getSetbackTeleportUtil().shouldBlockMovement()) {
            rewardVL();
            updateBlinkTimestamps();
            return;
        }

        if (isExempt(ExemptType.VEHICLE, ExemptType.CLIENT_ANTICHEAT, ExemptType.DIED, ExemptType.FLYING, ExemptType.RESPAWN,
                ExemptType.INVALID_GAMEMODE, ExemptType.LAGGING, ExemptType.LIQUID, ExemptType.ELYTRA_FLYING)) {
            updateBlinkTimestamps();
            return;
        }

        if (player.getRespawnTick() < 20L) {
            updateBlinkTimestamps();
            return;
        }

        if (player.getCheckManager().getCheck(MovementValidation.class).getOffset() > 0.5) {
            updateBlinkTimestamps();
            return;
        }

        final long currentTime = time();
        final int timeDiff = (int) (currentTime - lastBlinkTime);
        final double dis = player.deltaXZ;

        if (timeDiff > cancelThreshold && shouldCancelPacket && shouldModifyPackets()) {
            player.compensatedEntities.entitiesRemovedThisTick.clear();
            event.setCancelled(true);
        }

        if (timeDiff > maxBlinkTime && dis > 0.0032 && checkCooldown(currentTime, lastMove2Flag, 1000)) {

            if (timeDiff > maxBlinkTime) {
                if (++move2Buffer > maxMove2Buffer) {
                    if (flagAndAlert("(MOVE2)\nd= " + timeDiff + "\ns= " + dis)) {
                        if (shouldTeleportBack) performAntiCheatAction();
                        lastMove2Flag = currentTime;
                        move2Buffer = 0;
                    }
                    player.sendTransaction();
                }
            }
        } else {
            move2Buffer = Math.max(0, move2Buffer - 1);
        }
    }

    private boolean checkCooldown(long currentTime, long lastFlagTime, long cooldown) {
        return (currentTime - lastFlagTime) > cooldown;
    }

    private void performAntiCheatAction() {
        if (((!player.isOnGround()
                && !player.isLastOnGround()
                && onVoid(player.lastLocationData))
                || isExempt(ExemptType.VOID))
                && shouldSetBackToVoid) {
            if (player.getSetbackTeleportUtil().executeTeleport(player.getLastLocationData())) {
                needClean = true;
            }
        } else {
            if (player.getSetbackTeleportUtil().executeTeleport(loc.add(0, 1, 0))) {
                needClean = true;
            }
        }
        updateBlinkTimestamps();
        if (needClean) {
            player.compensatedEntities.entitiesRemovedThisTick.clear();
            needClean = false;
        }
    }

    private boolean isChatPacket(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.CHAT_MESSAGE ||
                packetType == PacketType.Play.Server.CHAT_MESSAGE ||
                packetType == PacketType.Play.Client.CHAT_COMMAND;
    }

    @Override
    public void reload() {
        super.reload();
        boolean mitigateEnable = getConfig().getBoolean(getConfigName() + ".mitigate.enable");
        shouldCancelPacket = getConfig().getBoolean(getConfigName() + ".mitigate.cancel.enable") && mitigateEnable;
        shouldTeleportBack = getConfig().getBoolean(getConfigName() + ".mitigate.teleport") && mitigateEnable;
        shouldSetBackToVoid = getConfig().getBoolean(getConfigName() + ".mitigate.to-void") && mitigateEnable;
        cancelThreshold = getConfig().getInt(getConfigName() + ".mitigate.cancel.time");
        maxBlinkTime = getConfig().getInt(getConfigName() + ".max-lagging-time");
        maxChatBuffer = getConfig().getInt(getConfigName() + ".buffer.chat");
        maxMove2Buffer = getConfig().getInt(getConfigName() + ".buffer.blink");
        maxMoveBuffer = getConfig().getInt(getConfigName() + ".buffer.flying");
        maxTrackingBuffer = getConfig().getInt(getConfigName() + ".buffer.attack");
        if (maxMove2Buffer < 0) {
            maxMove2Buffer = Integer.MAX_VALUE;
        }
        if (maxMoveBuffer < 0) {
            maxMoveBuffer = Integer.MAX_VALUE;
        }
        if (maxChatBuffer < 0) {
            maxChatBuffer = Integer.MAX_VALUE;
        }
    }

    private boolean onVoid(PacketLocation loc) {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_18)) {
            for (int y = loc.getBlockY(); y >= -70; y--) {
                final StateType type = player.getCompensatedWorld().getBlock(loc.toVector()).getType();
                if (!type.isAir()) {
                    return false;
                }
            }
        } else {
            for (int y = loc.getBlockY(); y >= 0; y--) {
                final StateType type = player.getCompensatedWorld().getBlock(loc.toVector()).getType();
                if (!type.isAir()) {
                    return false;
                }
            }
        }
        return true;
    }
}
