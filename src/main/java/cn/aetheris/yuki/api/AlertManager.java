package cn.aetheris.yuki.api;


import java.util.UUID;

public interface AlertManager {

    boolean hasAlertsEnabled(UUID uuid);

    void toggleAlerts(UUID uuid);

    boolean hasVerboseEnabled(UUID uuid);

    void toggleVerbose(UUID uuid);

    boolean debugEnabled(UUID uuid);

    void toggleDebug(UUID uuid);

    void togglePacketCancelDebug(UUID uuid);

    boolean hasPacketCancelDebugEnabled(UUID uuid);

    void toggleSyncDebug(UUID uuid);

    boolean hasSyncDebugEnabled(UUID uuid);

    void toggleMitigateDebug(UUID uuid);

    boolean hasMitigateDebugEnabled(UUID uuid);

    void toggleSetbackDebug(UUID uuid);

    boolean hasSetbackDebugEnabled(UUID uuid);
}