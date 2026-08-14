/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerLoginEvent
 *  org.slf4j.Logger
 */
package cn.dg32z.neko.listener.bukkit;

import cn.dg32z.annotation.Include;
import dev.jnic.bpgzuJ.JNICLoader;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.slf4j.Logger;

@Include
public final class PluginLoadListener
implements Listener {
    @Generated
    private static final Logger log;

    @EventHandler(priority=EventPriority.LOWEST)
    public native void onPlayerLogin(PlayerLoginEvent var1);

    static native /* synthetic */ void $jnicClinit();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        PluginLoadListener.$jnicLoader();
        PluginLoadListener.$jnicClinit();
    }
}
