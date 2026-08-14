/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cn.dg32z.libs.cn.chengzhimeow.ccyaml.configuration.yaml.YamlConfiguration
 *  lombok.Generated
 *  org.bukkit.plugin.java.JavaPlugin
 */
package cn.dg32z.neko;

import cn.dg32z.annotation.Include;
import cn.dg32z.libs.cn.chengzhimeow.ccyaml.configuration.yaml.YamlConfiguration;
import cn.dg32z.neko.manager.LibrariesManager;
import cn.dg32z.neko.manager.LicenseManager;
import cn.dg32z.neko.manager.PacketEventsManager;
import dev.jnic.bpgzuJ.JNICLoader;
import lombok.Generated;
import org.bukkit.plugin.java.JavaPlugin;

@Include
public final class NekoAntiCheat
extends JavaPlugin {
    public static boolean ENABLED;
    private static NekoAntiCheat instance;
    private YamlConfiguration messageConfig;
    private volatile boolean needToDisable = false;
    private Thread mainThread = Thread.currentThread();
    private LicenseManager licenseManager;
    private PacketEventsManager packetEventsManager;
    private LibrariesManager librariesManager;

    public native void onDisable();

    public native void unsafeSetEnabled(boolean var1);

    public native void onLoad();

    public native void onEnable();

    public native void console(String var1);

    @Generated
    public native YamlConfiguration getMessageConfig();

    @Generated
    public native boolean isNeedToDisable();

    @Generated
    public native Thread getMainThread();

    @Generated
    public native LicenseManager getLicenseManager();

    @Generated
    public native PacketEventsManager getPacketEventsManager();

    @Generated
    public native LibrariesManager getLibrariesManager();

    @Generated
    public native void setMessageConfig(YamlConfiguration var1);

    @Generated
    public native void setNeedToDisable(boolean var1);

    @Generated
    public native void setMainThread(Thread var1);

    @Generated
    public native void setLicenseManager(LicenseManager var1);

    @Generated
    public native void setPacketEventsManager(PacketEventsManager var1);

    @Generated
    public native void setLibrariesManager(LibrariesManager var1);

    @Generated
    public static native NekoAntiCheat getInstance();

    @Generated
    public static native void setInstance(NekoAntiCheat var0);

    private native /* synthetic */ void lambda$onEnable$1();

    private native /* synthetic */ void lambda$onLoad$0();

    static native /* synthetic */ void $jnicClinit();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        NekoAntiCheat.$jnicLoader();
        NekoAntiCheat.$jnicClinit();
    }
}
