/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package cn.dg32z.neko.api;

import cn.dg32z.neko.api.AlertManager;
import cn.dg32z.neko.api.PlayerAPI;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.entity.Player;

public interface AbstractAPI {
    public PlayerAPI getUser(Player var1);

    public PlayerAPI getUser(UUID var1);

    public String getServerName();

    @Deprecated
    public void setServerName(String var1);

    public void registerVariable(String var1, Function<PlayerAPI, String> var2);

    public void registerVariable(String var1, String var2);

    public String getVersion();

    public void registerFunction(String var1, Function<Object, Object> var2);

    public Function<Object, Object> getFunction(String var1);

    public void reload();

    public AlertManager getAlertManager();
}
