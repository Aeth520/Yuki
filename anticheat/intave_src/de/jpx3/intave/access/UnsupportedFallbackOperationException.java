/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.IntaveInternalException;

public final class UnsupportedFallbackOperationException
extends IntaveInternalException {
    public static final UnsupportedFallbackOperationException INSTANCE = new UnsupportedFallbackOperationException("Player gone already?");

    private UnsupportedFallbackOperationException() {
    }

    private UnsupportedFallbackOperationException(String message) {
        super(message);
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        super.setStackTrace(stackTrace);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }
}

