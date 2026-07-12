package cn.aetheris.yuki.api;

import java.util.Collection;
import java.util.UUID;

public interface AntiCheatUser {

    String getName();

    UUID getUniqueId();

    String getBrand();

    int getTransactionPing();

    int getCps();

    int getLastCps();

    int getKeepAlivePing();

    String getVersionName();

    double getHorizontalSensitivity();

    double getVerticalSensitivity();

    boolean isVanillaMath();

    boolean isTeleporting();

    boolean inVehicle();

    void updatePermissions();

    Collection<? extends AbstractCheck> getChecks();

    void runSafely(Runnable runnable);

    boolean isMoveLagging();

    boolean isFlyingLagging();

    double getTPS();

    String getChannel();

    boolean isClientACUser();

    void mitigateDamage(String name);

    int getLastTransactionSent();

    int getLastTransactionReceived();

    void addRealTimeTask(int transaction, Runnable runnable);

    void addRealTimeTaskAsync(int transaction, Runnable runnable);

    int calculateSensitivity();
}
