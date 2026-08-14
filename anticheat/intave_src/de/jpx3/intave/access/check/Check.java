/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access.check;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Check {
    ATTACK_RAYTRACE("AttackRaytrace"),
    BREAK_SPEED_LIMITER("BreakSpeedLimiter"),
    CLICK_PATTERNS("ClickPatterns"),
    CLICK_SPEED_LIMITER("ClickSpeedLimiter"),
    HEURISTICS("Heuristics"),
    INTERACTION_RAYTRACE("InteractionRaytrace"),
    INVENTORY_CLICK_ANALYSIS("InventoryClickAnalysis"),
    PHYSICS("Physics"),
    PLACEMENT_ANALYSIS("PlacementAnalysis"),
    PROTOCOL_SCANNER("ProtocolScanner"),
    TIMER("Timer");

    private static final Map<String, Check> LOOKUP_MAP;
    private final String typeName;

    private Check(String name) {
        this.typeName = name;
    }

    public static Check fromName(String name) {
        return LOOKUP_MAP.get(name.toLowerCase(Locale.ROOT));
    }

    public String typeName() {
        return this.typeName;
    }

    static {
        LOOKUP_MAP = new HashMap<String, Check>();
        for (Check value : Check.values()) {
            LOOKUP_MAP.put(value.typeName.toLowerCase(Locale.ROOT), value);
        }
    }
}

