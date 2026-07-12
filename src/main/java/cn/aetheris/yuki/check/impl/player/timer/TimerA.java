package cn.aetheris.yuki.check.impl.player.timer;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.concurrent.TimeUnit;

@CheckData(name = "TimerA (Balance)",
        configName = "TimerA",
        description = "Faster Game Speed",
        type = CheckType.TIMER,
        setback = 12)
public class TimerA extends Check implements PacketCheck {
    long timerBalanceRealTime = 0;

    
    long knownPlayerClockTime = (long) (System.nanoTime() - 6e10);
    long lastMovementPlayerClock = (long) (System.nanoTime() - 6e10);

    
    
    long clockDrift;
    
    long limitAbuseOverPing;

    long lastFlag;

    boolean hasGottenMovementAfterTransaction = false;


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    
    
    
    
    
    
    public TimerA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

        if (hasGottenMovementAfterTransaction && isTransaction(event.getPacketType())) {
            knownPlayerClockTime = lastMovementPlayerClock;
            lastMovementPlayerClock = player.getPlayerClockAtLeast();
            hasGottenMovementAfterTransaction = false;
        }

        if (!shouldCountPacketForTimer(event.getPacketType())) return;

        hasGottenMovementAfterTransaction = true;
        timerBalanceRealTime += (long) 50e6;

        doCheck(event);
    }


    public void doCheck(final PacketReceiveEvent event) {
        double transactionPing = player.getTransactionPing();
        
        long diff = timerBalanceRealTime - System.nanoTime();
        boolean needsAdjustment = limitAbuseOverPing != -1 && transactionPing >= limitAbuseOverPing;
        boolean wouldFailNormal = timerBalanceRealTime > System.nanoTime();
        boolean failsAdjusted = needsAdjustment && (timerBalanceRealTime + ((transactionPing * 1e6) - clockDrift - 100e6)) > System.nanoTime();

        if (event.isCancelled()) {
            return;
        }

        if (wouldFailNormal || failsAdjusted) {
            
            timerBalanceRealTime -= (long) 50e6;
            if (time() - lastFlag < 500L) {
                return;
            }
            if (flag()) {
                lastFlag = time();
                
                
                if (wouldFailNormal && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }

                if (isAboveSetbackVl()) player.getSetbackTeleportUtil().executeNonSimulatingSetback();

                if (wouldFailNormal) {


                    alert("timeShit= " + TimeUnit.NANOSECONDS.toMillis(diff) + "ms");
                }
            }


        }

        timerBalanceRealTime = Math.max(timerBalanceRealTime, lastMovementPlayerClock - clockDrift);
    }

    public boolean shouldCountPacketForTimer(PacketTypeCommon packetType) {
        
        return isTickPacket(packetType);
    }

    @Override
    public void reload() {
        super.reload();
        clockDrift = (long) (getConfig().getDoubleElse(getConfigName() + ".drift", 120.0) * 1e6);
        limitAbuseOverPing = (long) (getConfig().getDoubleElse(getConfigName() + ".ping-abuse-limit-threshold", 1000) * 1e6);
    }
}