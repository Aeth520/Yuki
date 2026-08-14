/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access.player;

import java.util.function.Consumer;

public interface PlayerClicks {
    public int clicksLastSecond();

    public void subscribeToSecond(Consumer<Integer> var1);
}

