package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AlertManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
public final class AlertManagerImpl implements AlertManager {

    private final Set<UUID> enabledAlerts = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledVerbose = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledDebug = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledSync = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledSetBack = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledMitigate = ConcurrentHashMap.newKeySet();
    private final Set<UUID> enabledPacketCancel = ConcurrentHashMap.newKeySet();


    @Override
    public boolean hasAlertsEnabled(UUID uuid) {
        return enabledAlerts.contains(uuid);
    }

    @Override
    public void toggleAlerts(UUID uuid) {
        toggleSetting(enabledAlerts, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("output.alerts.enable"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("output.alerts.disable")
        );
    }

    @Override
    public boolean hasVerboseEnabled(UUID uuid) {
        return enabledVerbose.contains(uuid);
    }

    @Override
    public void toggleVerbose(UUID uuid) {
        toggleSetting(enabledVerbose, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("output.verbose.enable"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("output.verbose.disable")
        );
    }


    @Override
    public boolean debugEnabled(UUID uuid) {
        return enabledDebug.contains(uuid);
    }

    @Override
    public void toggleDebug(UUID uuid) {
        toggleSetting(enabledDebug, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.enable").replace("%type%", "normal"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.disable").replace("%type%", "normal")
        );
    }

    @Override
    public void togglePacketCancelDebug(UUID uuid) {
        toggleSetting(enabledPacketCancel, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.enable").replace("%type%", "cancel"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.disable").replace("%type%", "cancel")
        );
    }

    @Override
    public boolean hasPacketCancelDebugEnabled(UUID uuid) {
        return enabledPacketCancel.contains(uuid);
    }

    @Override
    public void toggleSyncDebug(UUID uuid) {
        toggleSetting(enabledSync, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.enable").replace("%type%", "sync"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.disable").replace("%type%", "sync")
        );
    }

    @Override
    public boolean hasSyncDebugEnabled(UUID uuid) {
        return enabledSync.contains(uuid);
    }

    @Override
    public void toggleMitigateDebug(UUID uuid) {
        toggleSetting(enabledMitigate, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.enable").replace("%type%", "mitigate"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.disable").replace("%type%", "mitigate")
        );
    }

    @Override
    public boolean hasMitigateDebugEnabled(UUID uuid) {
        return enabledMitigate.contains(uuid);
    }

    @Override
    public void toggleSetbackDebug(UUID uuid) {
        toggleSetting(enabledSetBack, uuid,
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.enable").replace("%type%", "setback"),
                () -> PluginLoader.INSTANCE.getLangManager().i18n("commands.debug.disable").replace("%type%", "setback")
        );
    }

    @Override
    public boolean hasSetbackDebugEnabled(UUID uuid) {
        return enabledSetBack.contains(uuid);
    }

    public void handlePlayerQuit(UUID uuid) {
        enabledAlerts.remove(uuid);
        enabledVerbose.remove(uuid);
        enabledDebug.remove(uuid);
        enabledSync.remove(uuid);
        enabledSetBack.remove(uuid);
        enabledMitigate.remove(uuid);
        enabledPacketCancel.remove(uuid);
    }

    private void toggleSetting(Set<UUID> setting, UUID uuid, Supplier<String> enableMsgSupplier, Supplier<String> disableMsgSupplier) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        if (setting.remove(uuid)) {
            player.sendMessage(disableMsgSupplier.get());
        } else {
            setting.add(player.getUniqueId());
            player.sendMessage(enableMsgSupplier.get());
        }
    }
}
