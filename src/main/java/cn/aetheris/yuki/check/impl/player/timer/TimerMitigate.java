package cn.aetheris.yuki.check.impl.player.timer;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "TimerMitigate (1/3)", configName = "TimerMitigate", description = "Check for timer", decay = 0.75)
public class TimerMitigate extends TimerA {

    long lastFlag;
    
    private long limitAbuseOverPing;

    public TimerMitigate(PlayerData player) {
        super(player);
    }

    @Override
    public void doCheck(final PacketReceiveEvent event) {
        
        if (player.getSetbackTeleportUtil().shouldBlockMovement() || player.isTeleporting()) {
            timerBalanceRealTime -= (long) 50e6;
            return;
        }
        if (timerBalanceRealTime > System.nanoTime()) {
            if (time() - lastFlag < 500L) {
                return;
            }
            
            if (!event.isCancelled()) {
                if (flagAndAlert()) {
                    if (isAboveSetbackVl()) {
                        player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                    }
                }
            }
            lastFlag = time();
            
            timerBalanceRealTime -= (long) 50e6;
        }

        limitFallBehind();
    }

    
    protected void limitFallBehind() {
        
        long playerClock = lastMovementPlayerClock;
        if (limitAbuseOverPing != -1 && System.nanoTime() - playerClock > limitAbuseOverPing) {
            playerClock = System.nanoTime() - limitAbuseOverPing;
        }
        timerBalanceRealTime = Math.max(timerBalanceRealTime, playerClock - clockDrift);
    }

    @Override
    public void reload() {
        limitAbuseOverPing = getConfig().getLongElse("mitigates.timer.ping-abuse-limit-threshold", 1000L);
        if (limitAbuseOverPing != -1) {
            limitAbuseOverPing *= (long) 1e6;
        }
    }
}
