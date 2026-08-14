/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.IntaveBootFailureException;

public final class IntaveResourceCompilationException
extends IntaveBootFailureException {
    public IntaveResourceCompilationException() {
    }

    public IntaveResourceCompilationException(String message) {
        super(message);
    }

    public IntaveResourceCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntaveResourceCompilationException(Throwable cause) {
        super(cause);
    }
}

