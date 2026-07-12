package cn.aetheris.yuki.listener.bukkit.hooks;

import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class MythicMobListener extends AbstractListener {

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        final PlayerData data = getData(player);

        if (data == null) {
            return;
        }
        if (MythicBukkit.inst().getMobManager().isMythicMob(event.getDamager())) {
            data.setSinceMythicMobTicks(0);
        }
    }
}
