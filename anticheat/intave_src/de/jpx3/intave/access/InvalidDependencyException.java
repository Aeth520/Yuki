/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.IntaveBootFailureException;

public final class InvalidDependencyException
extends IntaveBootFailureException {
    public InvalidDependencyException() {
    }

    public InvalidDependencyException(String message) {
        super(message);
    }

    public InvalidDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDependencyException(Throwable cause) {
        super(cause);
    }
}

