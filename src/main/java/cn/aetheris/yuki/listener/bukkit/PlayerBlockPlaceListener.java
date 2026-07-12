package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.combat.killaura.KillAuraD;
import cn.aetheris.yuki.check.impl.player.airplace.AirPlaceA;
import cn.aetheris.yuki.check.impl.player.fastplace.FastPlaceA;
import cn.aetheris.yuki.check.impl.player.scaffold.ScaffoldF;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerBlockPlaceListener extends AbstractListener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = getData(player);
        if (data == null) return;
        if (event.isCancelled()) {
            MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                data.cancelledBlockTicks = System.currentTimeMillis();
                final AirPlaceA placeA = data.getCheckManager().getCheck(AirPlaceA.class);
                final ScaffoldF scaffoldF = data.getCheckManager().getCheck(ScaffoldF.class);
                final FastPlaceA fastPlaceA = data.getCheckManager().getCheck(FastPlaceA.class);
                final KillAuraD killAuraD = data.getCheckManager().getCheck(KillAuraD.class);
                if (scaffoldF != null) scaffoldF.buffer = 0;
                if (fastPlaceA != null) fastPlaceA.buffer = 0;
                if (placeA != null) placeA.buffer = 0;
                if (event.canBuild() && event.getBlockPlaced().getType().isSolid()) {
                    if (killAuraD != null) killAuraD.lastDoStuffTime = System.currentTimeMillis();
                }
            });
        }
    }

}
