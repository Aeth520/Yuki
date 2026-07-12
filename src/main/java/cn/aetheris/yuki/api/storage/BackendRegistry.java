package cn.aetheris.yuki.api.storage;

import cn.aetheris.yuki.util.message.LogUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BackendRegistry {

    private static final BackendRegistry INSTANCE = new BackendRegistry();

    private final Map<String, Backend> backends = new ConcurrentHashMap<>();

    private BackendRegistry() {
    }

    public static BackendRegistry getInstance() {
        return INSTANCE;
    }

    public void register(Backend backend) {
        String type = backend.getType().toLowerCase();
        backends.put(type, backend);
        LogUtils.consolePrefixed("&7Registered storage backend: &b" + type);
    }

    public void unregister(String type) {
        backends.remove(type.toLowerCase());
    }

    public Backend get(String type) {
        return backends.get(type.toLowerCase());
    }

    public Backend getOrDefault(String type, String defaultType) {
        Backend backend = backends.get(type.toLowerCase());
        if (backend == null) {
            backend = backends.get(defaultType.toLowerCase());
        }
        return backend;
    }

    public Set<String> getAvailableTypes() {
        return backends.keySet();
    }

    public void clear() {
        backends.clear();
    }
}
