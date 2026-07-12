package cn.aetheris.yuki.util.fake;

import cn.aetheris.yuki.util.message.ColorUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class FakeAntiCheatUtils {

    private static final Map<String, String> FAKE_MAP;
    private static final List<String> KEYS;
    private static final char[] RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    static {

        FAKE_MAP = Map.ofEntries(Map.entry("&cTatako", "&c"), Map.entry("&cIntave", "&c"), Map.entry("&bKarhu&fAC", "&b"), Map.entry("&2Spartan", "&2"), Map.entry("&6Vulcan", "&6"), Map.entry("&eSparky", "&e"), Map.entry("&bEasyAntiCheat", "&b"), Map.entry("&bACE", "&b"), Map.entry("&bPolar", "&b"), Map.entry("&cMX", "&c"), Map.entry("&7[&bSpiter&7]", "&b"), Map.entry("&5GodEyes", "&5"), Map.entry("&cGodsEye", "&c"), Map.entry("&dNekoAntiCheat", "&d"), Map.entry("&4AntiCheat", "&4"), Map.entry("&6BetterAntiCheat", "&6"), Map.entry("&2Medusa", "&2"), Map.entry("&bGrim&fAC", "&b"), Map.entry("&bAntiCheatExpert", "&b"), Map.entry("&dArtemis", "&d"), Map.entry("&6AngleGuard", "&6"));
        KEYS = List.copyOf(FAKE_MAP.keySet());
    }

    public static String getRandomName() {
        return KEYS.get(ThreadLocalRandom.current().nextInt(KEYS.size()));
    }

    public static String getColorCode(String randomKey) {
        return FAKE_MAP.get(randomKey);
    }

    public static String getName(String randomKey) {
        return ColorUtils.color(randomKey);
    }

    public static String generateRandomString() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final char[] buffer = new char[6];

        for (int i = 0; i < 6; i++) {
            buffer[i] = RANDOM_CHARS[random.nextInt(RANDOM_CHARS.length)];
        }
        return new String(buffer);
    }

    public static int generateRandomInt() {
        return ThreadLocalRandom.current().nextInt(1, 9);
    }
}
