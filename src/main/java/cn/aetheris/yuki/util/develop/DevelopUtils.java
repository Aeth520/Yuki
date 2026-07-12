package cn.aetheris.yuki.util.develop;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DevelopUtils {

    private static final Set<UUID> ALLOWED_UUIDS = new HashSet<>();

    static {
        ALLOWED_UUIDS.add(UUID.fromString("93e73bda-13fb-49fb-aafe-2e50b8679209"));
        ALLOWED_UUIDS.add(UUID.fromString("464a979d-bc05-4742-933e-e5427b0a1538"));
        ALLOWED_UUIDS.add(UUID.fromString("f8d309c0-786e-4032-a72c-f6b8e6ce9a09"));
        ALLOWED_UUIDS.add(UUID.fromString("2041f93e-66eb-3f84-805a-59197a45a187"));
        ALLOWED_UUIDS.add(UUID.fromString("e97b8ba1-3627-300a-bbfc-8ad84de388e0"));
        ALLOWED_UUIDS.add(UUID.fromString("4a6103d5-8bb0-439b-8cda-22750f3356f6"));
        ALLOWED_UUIDS.add(UUID.fromString("d1579697-062b-4e73-a2cf-e1f399ee63cd"));
    }

    
    public static boolean isDeveloper(Player player) {
        return ALLOWED_UUIDS.contains(player.getUniqueId());
    }
}
