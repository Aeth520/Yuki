package cn.aetheris.yuki.api;

import cn.aetheris.yuki.api.event.EventBus;
import org.bukkit.entity.Player;

import java.util.function.Function;

public interface AbstractAPI {

    PlayerAPI getUser(Player player);

    void setServerName(String name);

    String getServerName();

    void registerVariable(String variable, Function<PlayerAPI, String> replacement);

    void registerVariable(String variable, String replacement);

    String getVersion();

    void registerFunction(String key, Function<Object, Object> function);

    Function<Object, Object> getFunction(String key);

    void reload();

    void reloadAsync();

    boolean hasStarted();

    EventBus getEventBus();

    AlertManager getAlertManager();
}