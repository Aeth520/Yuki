/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package de.jpx3.intave.access;

import de.jpx3.intave.access.check.Check;
import de.jpx3.intave.access.check.CheckAccess;
import de.jpx3.intave.access.check.UnknownCheckException;
import de.jpx3.intave.access.player.PlayerAccess;
import de.jpx3.intave.access.player.storage.StorageGateway;
import de.jpx3.intave.access.player.trust.TrustFactor;
import de.jpx3.intave.access.player.trust.TrustFactorResolver;
import de.jpx3.intave.access.server.ServerAccess;
import java.io.PrintStream;
import org.bukkit.entity.Player;

public interface IntaveAccess {
    public void setTrustFactorResolver(TrustFactorResolver var1);

    public void setDefaultTrustFactor(TrustFactor var1);

    public void subscribeOutputStream(PrintStream var1);

    public void unsubscribeOutputStream(PrintStream var1);

    public void setStorageGateway(StorageGateway var1);

    public PlayerAccess player(Player var1);

    public ServerAccess server();

    public CheckAccess check(String var1) throws UnknownCheckException;

    public CheckAccess check(Check var1);

    public void fallback(Object var1);
}

