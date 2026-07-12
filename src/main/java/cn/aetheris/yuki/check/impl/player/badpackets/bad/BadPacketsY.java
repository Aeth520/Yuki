package cn.aetheris.yuki.check.impl.player.badpackets.bad;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "BadPacketsY (Combined)",
        configName = "BadPacketsY",
        description = "Invalid packet content or order for interaction packets",
        decay = 0.5,
        experimental = true)
public final class BadPacketsY extends Check implements PacketCheck {

    private final boolean shouldCheck = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)
            && player.getClientVersion().isOlderThan(ClientVersion.V_1_20);

    private double lastX, lastY, lastZ;
    private boolean lastOnGround;

    private double buffer2;

    private boolean receivedMismatchedPosRot = false;
    private boolean lastPacketWasPosAndRot = false;


    public BadPacketsY(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (isFlying(packetType)) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            lastX = flying.getLocation().getX();
            lastY = flying.getLocation().getY();
            lastZ = flying.getLocation().getZ();
            lastOnGround = flying.isOnGround();
            receivedMismatchedPosRot = false;
            lastPacketWasPosAndRot = false;
        }

        
        if (packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerFlying posAndRot = new WrapperPlayClientPlayerFlying(event);
            double currentX = posAndRot.getLocation().getX();
            double currentY = posAndRot.getLocation().getY();
            double currentZ = posAndRot.getLocation().getZ();
            boolean isOnGround = posAndRot.isOnGround();

            if (shouldCheck) {
                if (currentX != lastX || currentY != lastY || currentZ != lastZ || lastOnGround != isOnGround) {
                    receivedMismatchedPosRot = true;
                }
            }

            lastPacketWasPosAndRot = true;
        }

        if (packetType == PacketType.Play.Client.USE_ITEM || packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            if (shouldCheck && !lastPacketWasPosAndRot) {
                if (buffer++ > 1) {
                    flagAndAlert("(TypeA)");
                }
            } else if (receivedMismatchedPosRot) {
                if (buffer2++ > 1) {
                    flagAndAlert("(TypeB)");
                    receivedMismatchedPosRot = false;
                }
            } else {
                rewardBufferAndVL();
                buffer2 = Math.max(buffer2 - getDecay(), 0);
                if (buffer2 == 0) {
                    rewardVL();
                }
            }
        }
    }
}
