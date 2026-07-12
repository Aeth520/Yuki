package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FeatureManager {

    @Getter
    private static final FeatureManager instance = new FeatureManager();

    private final Map<String, Boolean> defaults = new LinkedHashMap<>();
    private final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    private FeatureManager() {
        registerDefaults();
    }

    private void registerDefaults() {
        defaults.put("checks.movement", true);
        defaults.put("checks.combat", true);
        defaults.put("checks.scaffold", true);
        defaults.put("checks.player", true);
        defaults.put("checks.misc", true);
        defaults.put("checks.chat", true);
        defaults.put("checks.multiactions", true);
        defaults.put("performance.monitor", true);
        defaults.put("discord.webhook", false);
        defaults.put("dump.diagnostic", true);
    }

    public void loadFromConfig() {
        flags.clear();
        for (Map.Entry<String, Boolean> entry : defaults.entrySet()) {
            String key = entry.getKey();
            boolean value = PluginLoader.INSTANCE.getConfigManager()
                    .getConfig().getBooleanElse("features." + key, entry.getValue());
            flags.put(key, value);
        }
    }

    public boolean isEnabled(String key) {
        return flags.getOrDefault(key, defaults.getOrDefault(key, false));
    }

    public void setEnabled(String key, boolean enabled) {
        if (defaults.containsKey(key)) {
            flags.put(key, enabled);
        }
    }

    public void reset(String key) {
        if (defaults.containsKey(key)) {
            flags.put(key, defaults.get(key));
        }
    }

    public void resetAll() {
        flags.clear();
        flags.putAll(defaults);
    }

    public Map<String, Boolean> getAllFlags() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(flags));
    }

    public Map<String, Boolean> getDefaults() {
        return Collections.unmodifiableMap(defaults);
    }

    public boolean isRegistered(String key) {
        return defaults.containsKey(key);
    }

    public void reload() {
        loadFromConfig();
    }
}
