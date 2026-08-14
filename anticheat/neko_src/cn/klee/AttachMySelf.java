/*
 * Decompiled with CFR 0.152.
 */
package cn.klee;

import cn.dg32z.annotation.Include;
import dev.jnic.bpgzuJ.JNICLoader;

@Include
public class AttachMySelf {
    public static native void selfAttachPatch();

    public static native void doAttach();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        AttachMySelf.$jnicLoader();
    }
}
