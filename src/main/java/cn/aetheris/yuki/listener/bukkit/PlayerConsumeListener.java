package cn.aetheris.yuki.listener.bukkit;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.combat.killaura.KillAuraD;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumeListener extends AbstractListener {

    @EventHandler
    public void playerConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = getData(player);

        if (data == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            final KillAuraD check = data.getCheckManager().getCheck(KillAuraD.class);
            if (check == null) {
                return;
            }

            check.lastDoStuffTime = System.currentTimeMillis();
        });
    }
}
