/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cn.dg32z.neko.manager;

import cn.dg32z.annotation.Include;
import cn.dg32z.libs.com.github.retrooper.packetevents.PacketEventsAPI;
import cn.dg32z.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import dev.jnic.bpgzuJ.JNICLoader;
import lombok.Generated;

@Include
public final class PacketEventsManager {
    private PacketEventsAPI api;
    private ServerVersion serverVersion;

    public native void load();

    public native void disable();

    public native void init();

    @Generated
    public native PacketEventsAPI getApi();

    @Generated
    public native ServerVersion getServerVersion();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        PacketEventsManager.$jnicLoader();
    }
}
