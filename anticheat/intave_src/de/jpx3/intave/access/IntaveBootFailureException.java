/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

public class IntaveBootFailureException
extends RuntimeException {
    public IntaveBootFailureException() {
    }

    public IntaveBootFailureException(String message) {
        super(message);
    }

    public IntaveBootFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntaveBootFailureException(Throwable cause) {
        super(cause);
    }

    protected IntaveBootFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

