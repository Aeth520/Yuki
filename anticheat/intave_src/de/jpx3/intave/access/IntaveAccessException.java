/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.IntaveInternalException;

public class IntaveAccessException
extends IntaveInternalException {
    public IntaveAccessException() {
    }

    public IntaveAccessException(String message) {
        super(message);
    }

    public IntaveAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntaveAccessException(Throwable cause) {
        super(cause);
    }
}

