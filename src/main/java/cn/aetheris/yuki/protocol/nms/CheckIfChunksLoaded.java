package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.player.PlayerData;


public final class CheckIfChunksLoaded {

    

    public static boolean isChunksUnloadedAt(PlayerData player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxY < player.compensatedWorld.getMinHeight() || minY >= player.compensatedWorld.getMaxHeight()) {
            return true;
        }

        int chunkMinX = minX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int x = chunkMinX; x <= chunkMaxX; x++) {
            for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                if (player.compensatedWorld.getChunk(x, z) == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
