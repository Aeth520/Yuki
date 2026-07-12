package cn.aetheris.yuki.predictionengine.blockeffects;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.data.VectorData;
import cn.aetheris.yuki.protocol.nms.Collisions;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;

/**
 * Default block effects resolver, mirroring the inline bubble column mechanics
 * previously located in {@link Collisions#onInsideBlock}. Geyser effects target
 * the sulfur spike block (the sulfur geyser mechanic introduced in 1.21.6).
 *
 * <p>Note: {@code StateTypes.POTENT_SULFUR_GEYSER} is not present in PacketEvents 2.13.0;
 * the closest equivalent {@link StateTypes#SULFUR_SPIKE} is used instead.
 */
public class DefaultBlockEffectsResolver implements BlockEffectsResolver {

    private static final double GEYSER_UPWARD_FORCE = 0.15D;

    @Override
    public void applyGeyserEffects(PlayerData player, SimpleCollisionBox playerBox) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21_6)) return;

        Collisions.hasMaterial(player, playerBox, (Pair<WrappedBlockState, Vector3d> pair) -> {
            if (pair.first().getType() == StateTypes.SULFUR_SPIKE) {
                player.clientVelocity.setY(Math.max(player.clientVelocity.getY(), GEYSER_UPWARD_FORCE));
                player.fallDistance = 0;
            }
            return false;
        });
    }

    @Override
    public void applyBubbleColumnEffects(PlayerData player, SimpleCollisionBox playerBox) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_13)) return;

        Collisions.hasMaterial(player, playerBox, (Pair<WrappedBlockState, Vector3d> pair) -> {
            WrappedBlockState block = pair.first();
            if (block.getType() != StateTypes.BUBBLE_COLUMN) return false;

            Vector3d pos = pair.second();
            int blockX = (int) pos.getX();
            int blockY = (int) pos.getY();
            int blockZ = (int) pos.getZ();

            WrappedBlockState blockAbove = player.compensatedWorld.getBlock(blockX, blockY + 1, blockZ);
            boolean aboveIsAir = blockAbove.getType().isAir();
            boolean drag = block.isDrag();

            if (player.inVehicle() && player.compensatedEntities.self.getRiding().isBoat) {
                if (!aboveIsAir) {
                    if (drag) {
                        player.clientVelocity.setY(Math.max(-0.3D, player.clientVelocity.getY() - 0.03D));
                    } else {
                        player.clientVelocity.setY(Math.min(0.7D, player.clientVelocity.getY() + 0.06D));
                    }
                }
            } else {
                if (aboveIsAir) {
                    for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
                        if (drag) {
                            vector.vector.setY(Math.max(-0.9D, vector.vector.getY() - 0.03D));
                        } else {
                            vector.vector.setY(Math.min(1.8D, vector.vector.getY() + 0.1D));
                        }
                    }
                } else {
                    for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
                        if (drag) {
                            vector.vector.setY(Math.max(-0.3D, vector.vector.getY() - 0.03D));
                        } else {
                            vector.vector.setY(Math.min(0.7D, vector.vector.getY() + 0.06D));
                        }
                    }
                }
            }

            player.fallDistance = 0;
            return false;
        });
    }
}
