/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package cn.dg32z.neko.api;

import cn.dg32z.libs.net.kyori.adventure.text.Component;
import cn.dg32z.libs.org.jetbrains.annotations.Contract;
import cn.dg32z.libs.org.jetbrains.annotations.Nullable;
import cn.dg32z.neko.api.IHandler;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface PlayerAPI {
    public void enqueueRtTask(Runnable var1);

    public String getName();

    public UUID getUniqueId();

    public String getBrand();

    public int getTransactionPing();

    public int getCps();

    public int getLastCps();

    public int getKeepAlivePing();

    public String getVersionName();

    public double getHorizontalSensitivity();

    public double getVerticalSensitivity();

    public boolean isVanillaMath();

    public boolean isTeleporting();

    public boolean inVehicle();

    public void updatePermissions();

    public Collection<? extends IHandler> getChecks();

    @Contract(pure=true)
    public boolean supportsEndTick();

    @Contract(pure=true)
    public boolean canSkipTicks();

    @Contract(pure=true)
    public boolean supportFullTick();

    public void runSafely(Runnable var1);

    public boolean isMoveLagging();

    public boolean isFlyingDsync();

    public double getTPS();

    public String getChannel();

    public int getLastTransactionSent();

    public int getLastTransactionReceived();

    public void enqueueRtTask(int var1, Runnable var2);

    public int getLoadedChunks();

    @Deprecated
    default public int getLoaddedChunks() {
        return this.getLoadedChunks();
    }

    public void mitigateDamage(UUID var1);

    public void mitigateDamage(Player var1);

    public void addRealTimeTaskAsync(int var1, Runnable var2);

    @Nullable
    public String getBukkitWorldName();

    @Nullable
    public UUID getBukkitWorldUID();

    public void addRealTimeTask(int var1, Runnable var2);

    default public void addRealTimeTaskNow(Runnable runnable) {
        this.addRealTimeTask(this.getLastTransactionSent(), runnable);
    }

    default public void addRealTimeTaskNext(Runnable runnable) {
        this.addRealTimeTask(this.getLastTransactionSent() + 1, runnable);
    }

    public int calculateSensitivity();

    public void sendMessage(String var1);

    public void sendMessage(Component var1);

    @Contract
    public boolean isSlowdownByItem();

    public double getJumpResetProbability();

    public double getJumpResetAvgTiming();

    public double getJumpResetStd();
}
