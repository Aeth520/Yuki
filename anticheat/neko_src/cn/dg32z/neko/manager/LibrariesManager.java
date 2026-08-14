/*
 * Decompiled with CFR 0.152.
 */
package cn.dg32z.neko.manager;

import cn.chengzhiya.mhdflibrary.entity.RepositoryConfig;
import cn.dg32z.annotation.Include;
import dev.jnic.bpgzuJ.JNICLoader;

@Include
public final class LibrariesManager {
    private static Boolean nativeSupportAdventureApi;
    private static final String NEKO_COMMAND_VERSION = "1.0.0";
    private static final String NEKO_VECTOR_VERSION = "1.0.3";
    private static final String NEKO_SCHEDULER_VERSION = "1.4.0";
    private static final String NEKO_REFLECTION_VERSION = "1.4.12";
    private final RepositoryConfig packetevents = new RepositoryConfig("https://repo.grim.ac/snapshots/");
    private final RepositoryConfig nyacho = new RepositoryConfig("https://repo-eo.catnies.top/releases/");
    private final RepositoryConfig fastMath = new RepositoryConfig("https://repo.maven.apache.org/maven2/");
    private final RepositoryConfig config = new RepositoryConfig("https://nexus.scarsz.me/content/repositories/releases/");
    private final RepositoryConfig kyori = new RepositoryConfig("https://mvnrepository.com/artifact/net.kyori/adventure-api/");
    private final RepositoryConfig codemc = new RepositoryConfig("https://repo.codemc.io/repository/maven-public/");
    private final RepositoryConfig jitpack = new RepositoryConfig("https://jitpack.io/");
    private final RepositoryConfig maven2 = new RepositoryConfig("https://repo1.maven.org/maven2/");
    private final RepositoryConfig nekoReleases = new RepositoryConfig("http://43.249.192.127:8080/releases/");

    public static native boolean isNativeSupportAdventureApi();

    private native String handleString(String var1);

    @Include
    public native void init();

    public static native /* synthetic */ void $jnicLoader();

    static {
        JNICLoader.init();
        LibrariesManager.$jnicLoader();
    }
}
