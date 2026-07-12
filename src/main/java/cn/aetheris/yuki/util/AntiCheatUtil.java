package cn.aetheris.yuki.util;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractAPI;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.AlertManager;
import cn.aetheris.yuki.api.PlayerAPI;
import cn.aetheris.yuki.api.event.EventBus;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.config.file.YamlConfiguration;
import cn.aetheris.yuki.util.fake.FakeAntiCheatUtils;
import cn.aetheris.yuki.util.message.ColorUtils;
import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Getter
public final class AntiCheatUtil implements AbstractAPI, Init {

    private final Map<String, Function<PlayerAPI, String>> variableReplacements;
    private final Map<String, String> staticReplacements;
    private final EventBus eventBus;

    public AntiCheatUtil() {
        variableReplacements = new ConcurrentHashMap<>();
        staticReplacements = new ConcurrentHashMap<>();
        eventBus = new EventBus();
    }

    
    @Nullable
    @Override
    public PlayerAPI getUser(Player player) {
        return PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(player);
    }

    @Override
    public String getServerName() {
        return HookInit.getPlaceholderAPIHook().setPlaceholders(null, PluginLoader.INSTANCE.getLangManger().i18nWithoutPrefix("database-manager.server-name"));
    }

    
    @Override
    public void setServerName(String name) {
        variableReplacements.put("%server%", user -> name);
        final File dbFile = PluginLoader.INSTANCE.getConfigManager().getDatabaseFile();
        final YamlConfiguration database = YamlConfiguration.loadConfiguration(dbFile);
        database.set("database-manager.server-name", name);
        try {
            database.save(dbFile);
        } catch (IOException ignored) {
        }
    }

    
    public String replaceVariables(@Nullable PlayerAPI playerAPI, String content, boolean colors) {
        if (content == null) return null;
        String result = content;

        for (Map.Entry<String, String> entry : staticReplacements.entrySet()) {
            final String key = entry.getKey();
            final String replacement = entry.getValue() == null ? "" : entry.getValue();
            if (key != null && !key.isEmpty()) {
                result = result.replace(key, replacement);
            }
        }

        if (playerAPI != null) {
            for (Map.Entry<String, Function<PlayerAPI, String>> entry : variableReplacements.entrySet()) {
                final String key = entry.getKey();
                if (key == null || key.isEmpty()) continue;
                final Function<PlayerAPI, String> fn = entry.getValue();
                if (fn == null) continue;
                String replacement;
                try {
                    replacement = fn.apply(playerAPI);
                } catch (Exception ignored) {
                    replacement = "";
                }
                if (replacement == null) replacement = "";
                result = result.replace(key, replacement);
            }
        } else {
            for (String key : variableReplacements.keySet()) {
                if (key != null && !key.isEmpty()) result = result.replace(key, "");
            }
        }

        if (colors) {
            result = ColorUtils.color(result);
        }
        return ColorUtils.translateHexCodes(result);
    }

    
    @Override
    public AlertManager getAlertManager() {
        return PluginLoader.INSTANCE.getAlertManager();
    }

    
    @Override
    public void registerVariable(String variable, Function<PlayerAPI, String> replacement) {
        variableReplacements.put(variable, replacement);
    }

    
    @Override
    public void registerVariable(String variable, String replacement) {
        if (variable == null) return;
        staticReplacements.put(variable, replacement == null ? "" : replacement);
    }

    
    @Override
    public String getVersion() {
        return PluginLoader.INSTANCE.getVersion();
    }

    
    @Override
    public void registerFunction(String key, Function<Object, Object> function) {
    }

    
    @Override
    public Function<Object, Object> getFunction(String key) {
        return null;
    }

    
    @Override
    public void reload() {
        PluginLoader.INSTANCE.getConfigManager().reload();
        Check.clearMaxVLCache();
        for (PlayerData playerData : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
            ChannelHelper.runInEventLoop(playerData.user.getChannel(), () -> {
                playerData.onReload();
                playerData.updatePermissions();
                playerData.punishmentManager.reload();
                for (AbstractCheck abstractCheck : playerData.checkManager.allChecks.values()) {
                    abstractCheck.reload();
                }
            });
        }
        PluginLoader.INSTANCE.getSpectateManager().init();
        PluginLoader.INSTANCE.getExternalAPI().init();
    }

    @Override
    public void reloadAsync() {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), this::reload);
    }

    @Override
    public boolean hasStarted() {
        return Yuki.isEnablePlugin();
    }

    
    @Override
    public void init() {
        variableReplacements.put("%player%", PlayerAPI::getName);
        variableReplacements.put("%uuid%", user -> user.getUniqueId().toString());
        variableReplacements.put("%mix_ping%", user -> user.getTransactionPing() + "/" + user.getKeepAlivePing());
        variableReplacements.put("%transaction_ping%", user -> user.getTransactionPing() + "");
        variableReplacements.put("%keepalive_ping%", user -> user.getKeepAlivePing() + "");
        variableReplacements.put("%brand%", PlayerAPI::getBrand);
        variableReplacements.put("%channel%", PlayerAPI::getChannel);
        variableReplacements.put("%h_sensitivity%", user -> (int) Math.round(user.getHorizontalSensitivity() * 200) + "");
        variableReplacements.put("%v_sensitivity%", user -> (int) Math.round(user.getVerticalSensitivity() * 200) + "");
        variableReplacements.put("%wrapper_sensitivity%", user -> user.calculateSensitivity() + "%");
        variableReplacements.put("%fast_math%", user -> String.valueOf(!user.isVanillaMath()));
        variableReplacements.put("%tps%", user -> String.format("%.2f", user.getTPS()));
        variableReplacements.put("%version%", PlayerAPI::getVersionName);
        variableReplacements.put("%cps%", user -> String.valueOf(user.getCps()));
        variableReplacements.put("%last_cps%", user -> String.valueOf(user.getLastCps()));
        variableReplacements.put("%teleporting%", user -> String.valueOf(user.isTeleporting()));
        variableReplacements.put("%riding%", user -> String.valueOf(user.inVehicle()));
        variableReplacements.put("%world%", PlayerAPI::getBukkitWorldName);
        variableReplacements.put("%lagging%", user -> String.valueOf(user.isFlyingLagging()));
        variableReplacements.put("%move_lagging%", user -> String.valueOf(user.isMoveLagging()));
        variableReplacements.put("%prefix%", user -> ColorUtils.color(PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("anticheat-prefix", "&3Yuki &8» &f")));
        variableReplacements.put("%server%", user -> getServerName());
        variableReplacements.put("%random_string%", user -> FakeAntiCheatUtils.generateRandomString());
        variableReplacements.put("%random_int%", user -> FakeAntiCheatUtils.generateRandomInt() + "");
    }
}
