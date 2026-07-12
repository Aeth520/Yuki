package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.api.PlayerAPI;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.events.FlagEvent;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener extends AbstractListener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = getData(player);
        if (data == null) return;

        if (!event.isCancelled()) return;

        data.setSinceBukkitCancelMovementTicks(0);
    }

    @EventHandler
    public void FlagEvent(FlagEvent event) {
        final PlayerAPI api = event.getPlayer();
        final PlayerData data = getData(api.getUniqueId());
        if (data == null) return;

        if (data.getSinceBukkitCancelMovementTicks() < 5L) {
            if (event.isCancelled()) return;
            if (event.getCheckType() == CheckType.MOVEMENT_VALIDATION
                    || event.getCheckType() == CheckType.GROUNDSPOOF
                    || event.getCheckType() == CheckType.VELOCITY
                    || event.getCheckType() == CheckType.INVENTORY
                    || event.getCheckType() == CheckType.BLINK) {
                event.setCancelled(true);
            }
        }
    }
}
