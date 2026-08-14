/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.access.IntaveAccess;
import de.jpx3.intave.access.IntaveColdException;
import java.lang.ref.WeakReference;

public final class IntaveAccessor {
    private static WeakReference<IntaveAccess> weakAccess;

    public static synchronized boolean loaded() {
        IntavePlugin plugin = IntavePlugin.singletonInstance();
        return plugin != null && plugin.isEnabled() && IntaveAccessor.uncheckedUnsafeAccess() != null;
    }

    public static synchronized WeakReference<IntaveAccess> weakAccess() {
        if (!IntaveAccessor.loaded()) {
            throw new IntaveColdException("Intave offline");
        }
        if (weakAccess == null) {
            weakAccess = new WeakReference<IntaveAccess>(IntaveAccessor.uncheckedUnsafeAccess());
        }
        return weakAccess;
    }

    public static synchronized IntaveAccess unsafeAccess() {
        if (!IntaveAccessor.loaded()) {
            throw new IntaveColdException("Intave offline");
        }
        return IntaveAccessor.uncheckedUnsafeAccess();
    }

    private static IntaveAccess uncheckedUnsafeAccess() {
        return IntavePlugin.singletonInstance().access();
    }
}

