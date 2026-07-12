package cn.aetheris.yuki.data.player;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeData {

    private static final Set<UUID> frozenPlayers = new HashSet<>();

    public static void setFrozen(Player player, boolean frozen) {
        if (!frozen) {
            frozenPlayers.remove(player.getUniqueId());
            return;
        }
        if (isFrozen(player)) {
            return;
        }
        frozenPlayers.add(player.getUniqueId());
    }

    public static boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }
}
