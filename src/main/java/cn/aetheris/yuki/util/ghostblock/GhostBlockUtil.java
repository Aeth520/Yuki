package cn.aetheris.yuki.util.ghostblock;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class GhostBlockUtil {
    public static boolean isGhostBlock(PlayerData player) {
        
        if (player.uncertaintyHandler.isOrWasNearGlitchyBlock) {
            return true;
        }

        
        
        
        
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
            SimpleCollisionBox largeExpandedBB = player.boundingBox.copy().expand(12, 0.5, 12);

            for (PacketEntity entity : player.compensatedEntities.entityMap.values()) {
                if (entity.isBoat()) {
                    if (entity.getPossibleCollisionBoxes().isIntersected(largeExpandedBB)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
