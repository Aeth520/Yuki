package cn.aetheris.yuki.listener.bukkit;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;

public final class PlayerAnimationListener extends AbstractListener {


    @EventHandler(priority = EventPriority.MONITOR)
    private void onClick(PlayerAnimationEvent event) {
        if (!PluginLoader.INSTANCE.getConfigManager().getConfig().getString("function.click-listener.mode").contains("bukkit")) {
            return;
        }

        final PlayerData data = getData(event.getPlayer());

        if (data == null) {
            return;
        }

        if (data.getExemptProcessor().isExempt(ExemptType.INTERACT, ExemptType.LAGGING, ExemptType.PLACING)) {
            data.getClickProcessor().setSwings(0);
        }

        data.getClickProcessor().setSwings(data.getClickProcessor().getSwings() + 1);
        if (data.getClickProcessor().getLastSwing() > 0L) {
            data.getClickProcessor().setDelay(time() - data.getClickProcessor().getLastSwing());
            if (!data.getClickProcessor().getSamples().isEmpty()) {
                data.getClickProcessor().getSamples().add(data.getClickProcessor().getDelay());
            }
        }
        data.getClickProcessor().setLastSwing(time());
    }

    private long time() {
        return System.currentTimeMillis();
    }
}
