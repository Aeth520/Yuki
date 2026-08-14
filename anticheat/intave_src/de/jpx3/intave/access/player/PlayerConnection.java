/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.intave.access.player;

import java.util.function.BiConsumer;

public interface PlayerConnection {
    public int latency();

    public int latencyJitter();

    public void subscribe(BiConsumer<Integer, Integer> var1);

    public long packetSentByClient();

    public long packetSentToClient();
}

