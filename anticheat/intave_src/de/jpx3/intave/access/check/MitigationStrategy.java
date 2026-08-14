/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access.check;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum MitigationStrategy {
    AGGRESSIVE("AGGRESSIVE"),
    CAREFUL("CAREFUL"),
    LENIENT("LENIENT"),
    SILENT("SILENT"),
    BARELY("BARELY"),
    NOT_SUPPORTED("");

    private static final Map<String, MitigationStrategy> BY_NAME;
    private final String name;

    public static MitigationStrategy byName(String name) {
        MitigationStrategy mitigationStrategy = BY_NAME.get(name.toUpperCase(Locale.ROOT));
        if (mitigationStrategy == null) {
            mitigationStrategy = NOT_SUPPORTED;
        }
        return mitigationStrategy;
    }

    private MitigationStrategy(String name) {
        this.name = name;
    }

    static {
        BY_NAME = new HashMap<String, MitigationStrategy>();
        Arrays.stream(MitigationStrategy.values()).forEach(value -> BY_NAME.put(value.name, (MitigationStrategy)((Object)value)));
    }
}

