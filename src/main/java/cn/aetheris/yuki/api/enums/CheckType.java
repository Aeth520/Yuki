package cn.aetheris.yuki.api.enums;

/**
 * Pure enum representing the category of an anti-cheat check.
 * Platform-specific bindings (e.g. Bukkit Material icons for GUI display)
 * are kept out of the API package to avoid leaking platform dependencies.
 */
public enum CheckType {

    ELYTRA,
    GROUNDSPOOF,
    NOSLOW,
    MOVEMENT_VALIDATION,
    SPRINT,
    VEHICLE,

    AIM,
    ANALYSIS,
    AUTOBLOCK,
    KILLAURA,
    REACH,
    VELOCITY,
    CLIENT,
    SPAM,
    AIRPLACE,
    AUTOCLICKER,
    AUTOTOTEM,
    BADPACKETS,
    BARITONE,
    BLINK,
    BREAK,
    CRASH,
    ENTITY,
    EXPLOIT,
    IMPOSSIBLE,
    INTERACT,
    FASTPLACE,
    INVENTORY,
    PINGSPOOF,
    POST,
    SCAFFOLD,
    TIMER,
    XRAY,
    MULTIACTIONS,
    NONE,
    CHAT,
    BEDROCK;

    public static final CheckType[] MOVEMENT = {
            ELYTRA, GROUNDSPOOF, NOSLOW, MOVEMENT_VALIDATION, SPRINT, VEHICLE
    };

    public static final CheckType[] COMBAT = {
            AIM, ANALYSIS, AUTOBLOCK, KILLAURA, REACH, VELOCITY
    };

    public static final CheckType[] MISC = {
            CLIENT, SPAM, CHAT
    };

    public static final CheckType[] PLAYER = {
            AIRPLACE, AUTOCLICKER, AUTOTOTEM, BADPACKETS, BARITONE, BLINK, BREAK,
            CRASH, ENTITY, EXPLOIT, IMPOSSIBLE, INTERACT, FASTPLACE, INVENTORY,
            PINGSPOOF, POST, SCAFFOLD, TIMER, XRAY, MULTIACTIONS
    };
}
