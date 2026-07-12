package cn.aetheris.yuki.predictionengine.blockeffects;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.protocol.nms.Collisions;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;

/**
 * Block effects resolver for 1.21.6+ clients. The sulfur geyser (sulfur spike)
 * mechanics introduced in 1.21.6 apply a stronger upward force than the legacy default.
 */
public class BlockEffectsResolverV1_21_6 extends DefaultBlockEffectsResolver {

    private static final double GEYSER_UPWARD_FORCE = 0.35D;

    @Override
    public void applyGeyserEffects(PlayerData player, SimpleCollisionBox playerBox) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21_6)) {
            super.applyGeyserEffects(player, playerBox);
            return;
        }

        Collisions.hasMaterial(player, playerBox, (Pair<WrappedBlockState, Vector3d> pair) -> {
            if (pair.first().getType() == StateTypes.SULFUR_SPIKE) {
                player.clientVelocity.setY(Math.max(player.clientVelocity.getY(), GEYSER_UPWARD_FORCE));
                player.fallDistance = 0;
            }
            return false;
        });
    }
}
