/*
 * Decompiled with CFR 0.152.
 */
package cn.dg32z.neko;

import cn.dg32z.annotation.Include;
import dev.jnic.bpgzuJ.JNICLoader;

@Include
public class Signaller {
    private static native Object globalLocker();

    public static native void signal(String var0, Object var1);

    public static native <T> T replyBlocked(String var0, boolean var1);

    public static native Object replyImmediately(String var0, boolean var1);

    static native /* synthetic */ void $jnicClinit();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        Signaller.$jnicLoader();
        Signaller.$jnicClinit();
    }
}
