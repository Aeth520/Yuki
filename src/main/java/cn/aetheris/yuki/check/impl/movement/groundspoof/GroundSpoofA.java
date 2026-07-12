package cn.aetheris.yuki.check.impl.movement.groundspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import cn.aetheris.yuki.protocol.nms.BlockUtils;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.protocol.nms.GetBoundingBox;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "GroundSpoofA", configName = "GroundSpoofA", type = CheckType.GROUNDSPOOF, description = "The player is trying to avoid taking damage (groundspoof)", setback = 5)
public final class GroundSpoofA extends Check implements PacketCheck {

    public boolean flipPlayerGroundStatus = false;
    private boolean isIgnoreGhostBlock;

    public GroundSpoofA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_FLYING || packetType == PacketType.Play.Client.PLAYER_ROTATION) {
            handlePlayerFlyingOrRotation(event);
        } else if (WrapperPlayClientPlayerFlying.isFlying(packetType)) {
            handlePlayerFlying(event);
        }
    }

    private void handlePlayerFlyingOrRotation(PacketReceiveEvent event) {
        if (player.getSetbackTeleportUtil().insideUnloadedChunk() || player.getSetbackTeleportUtil().blockOffsets) {
            return;
        }

        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        if (wrapper.isOnGround() && !wrapper.hasPositionChanged()) {
            if (!isNearGround()) {

                if (isExempt(ExemptType.VEHICLE,
                        ExemptType.VEHICLE_DIED,
                        ExemptType.GSIT_ACTION,
                        ExemptType.WEAPON_SHOOT,
                        ExemptType.CLIENT_ANTICHEAT))
                    return;

                if (GhostBlockUtil.isGhostBlock(player) && !isIgnoreGhostBlock) return;

                if (buffer++ > 4) {
                    if (flagAndAlertWithSetback("g= " + player.clientClaimsLastOnGround
                            + "\nw= " + wrapper.isOnGround()
                            + "\ni= " + isIgnoreGhostBlock
                            + "\ns= " + isAboveSetbackVl()
                            + "\nn= " + isNearGround()
                            + "\nx= " + player.x
                            + "\ny= " + player.y
                            + "\nz= " + player.z)) {
                        wrapper.setOnGround(false);
                        event.markForReEncode(true);
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }

    private void handlePlayerFlying(PacketReceiveEvent event) {
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        if (flipPlayerGroundStatus) {
            if (shouldModifyPackets()) {
                final MovementValidation movementValidation = player.getCheckManager().getCheck(MovementValidation.class);
                wrapper.setOnGround(!wrapper.isOnGround());
                event.markForReEncode(true);
                if (movementValidation != null) {
                    if (movementValidation.refreshBlocks) {
                        if (player.bukkitPlayer != null) {
                            BlockUtils.refreshBlocksAroundPlayer(player, player.getLocationData().toLocation(player.bukkitPlayer));
                        }
                    }
                }
                flipPlayerGroundStatus = false;
            }

            if (player.packetStateData.lastPacketWasTeleport && shouldModifyPackets()) {
                wrapper.setOnGround(false);
                event.markForReEncode(true);
            }

        }
    }

    private boolean isNearGround() {
        SimpleCollisionBox feetBB = GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.x, player.y, player.z, 0.6f, 0.001f);
        feetBB.expand(player.getMovementThreshold());

        return checkForBoxes(feetBB);
    }

    private boolean checkForBoxes(SimpleCollisionBox playerBB) {
        List<SimpleCollisionBox> boxes = new ArrayList<>();
        Collisions.getCollisionBoxes(player, playerBB, boxes, false);

        for (SimpleCollisionBox box : boxes) {
            if (playerBB.collidesVertically(box)) {
                return true;
            }
        }

        return player.compensatedWorld.isNearHardEntity(playerBB.copy().expand(4));
    }


    @Override
    public void reload() {
        super.reload();
        isIgnoreGhostBlock = getConfig().getBooleanElse(getConfigName() + ".ignore-ghost", true);
    }
}