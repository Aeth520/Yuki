/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.IntaveAccessException;

public final class IntaveColdException
extends IntaveAccessException {
    public IntaveColdException() {
    }

    public IntaveColdException(String message) {
        super(message);
    }

    public IntaveColdException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntaveColdException(Throwable cause) {
        super(cause);
    }
}

