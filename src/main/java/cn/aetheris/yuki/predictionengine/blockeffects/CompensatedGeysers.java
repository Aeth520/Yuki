package cn.aetheris.yuki.predictionengine.blockeffects;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.Collisions;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Caches sulfur geyser (sulfur spike) block positions near the player so that
 * {@link #isNearGeyser(SimpleCollisionBox)} avoids re-scanning the world each tick.
 */
public final class CompensatedGeysers {

    private final LongSet geyserPositions = new LongOpenHashSet();

    /**
     * Rescan the given box for sulfur spikes, refreshing the cached position set.
     */
    public void update(PlayerData player, SimpleCollisionBox scanBox) {
        geyserPositions.clear();
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_21_6)) return;

        Collisions.hasMaterial(player, scanBox, (Pair<WrappedBlockState, Vector3d> pair) -> {
            if (pair.first().getType() == StateTypes.SULFUR_SPIKE) {
                Vector3d pos = pair.second();
                geyserPositions.add(MathUtil.asLong((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()));
            }
            return false;
        });
    }

    /**
     * Quick check whether any cached geyser block intersects the given box.
     */
    public boolean isNearGeyser(SimpleCollisionBox box) {
        for (long packed : geyserPositions) {
            int x = unpackX(packed);
            int y = MathUtil.unpackY(packed);
            int z = MathUtil.unpackZ(packed);
            if (box.maxX > x && box.minX < x + 1
                    && box.maxY > y && box.minY < y + 1
                    && box.maxZ > z && box.minZ < z + 1) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        geyserPositions.clear();
    }

    private static int unpackX(long packed) {
        return (int) (packed >> 42);
    }
}
