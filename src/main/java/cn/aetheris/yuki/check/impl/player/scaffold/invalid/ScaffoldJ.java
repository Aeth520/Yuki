package cn.aetheris.yuki.check.impl.player.scaffold.invalid;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.materials.Materials;
import cn.aetheris.yuki.util.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "ScaffoldJ (Exapand)",
        configName = "ScaffoldJ",
        description = "Check for exapand scaffold",
        type = CheckType.SCAFFOLD,
        decay = 0.75)
public final class ScaffoldJ extends BlockPlaceCheck {

    public ScaffoldJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;
        if (Materials.isGlassPane(place.getMaterial())
                || Materials.isGate(place.getMaterial())) return;

        SimpleCollisionBox combined = getCombinedBox(place);

        final double[] possibleEyeHeights = player.getPossibleEyeHeights();
        double minEyeHeight = Double.MAX_VALUE;
        double maxEyeHeight = Double.MIN_VALUE;
        for (double height : possibleEyeHeights) {
            minEyeHeight = Math.min(minEyeHeight, height);
            maxEyeHeight = Math.max(maxEyeHeight, height);
        }

        double movementThreshold = !player.packetStateData.didLastMovementIncludePosition
                || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                ? player.getMovementThreshold() : 0;

        SimpleCollisionBox eyePositions = new SimpleCollisionBox(player.x, player.y + minEyeHeight, player.z, player.x, player.y + maxEyeHeight, player.z);
        eyePositions.expand(movementThreshold);

        if (eyePositions.isIntersected(combined)) {
            return;
        }

        boolean flag = switch (place.getFace()) {
            case NORTH -> eyePositions.minZ > combined.minZ;
            case SOUTH -> eyePositions.maxZ < combined.maxZ;
            case EAST -> eyePositions.maxX < combined.maxX;
            case WEST -> eyePositions.minX > combined.minX;
            case UP -> eyePositions.maxY < combined.maxY;
            case DOWN -> eyePositions.minY > combined.minY;
            default -> false;
        };

        if (flag) {
            if (buffer++ > 5) {
                if (flagAndAlert("face= " + place.getFace()) & shouldCancel()) {
                    place.resync();
                    rewardBufferAndVL();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
