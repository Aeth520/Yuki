package cn.aetheris.yuki.predictionengine.blockeffects;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;

/**
 * Resolves block effects that affect player movement (geysers, bubbles, etc.)
 * Abstraction allows version-specific implementations.
 */
public interface BlockEffectsResolver {
    /**
     * Check if the player is within a geyser block's effect range
     * and apply vertical velocity if so.
     */
    void applyGeyserEffects(PlayerData player, SimpleCollisionBox playerBox);

    /**
     * Check if the player is within bubble column range
     * and apply effects if so.
     */
    void applyBubbleColumnEffects(PlayerData player, SimpleCollisionBox playerBox);
}
