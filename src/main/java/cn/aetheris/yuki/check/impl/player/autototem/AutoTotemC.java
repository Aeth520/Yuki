package cn.aetheris.yuki.check.impl.player.autototem;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CheckData(name = "AutoTotemC (Pattern)",
        configName = "AutoTotemC",
        decay = 0.54,
        experimental = true)
public class AutoTotemC extends InventoryCheck {

    private final List<Long> timeIntervals = new ArrayList<>();

    public AutoTotemC(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow clickType = new WrapperPlayClientClickWindow(event);

            if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isOlderThan(ServerVersion.V_1_9)) {
                return;
            }

            final Player bukkitPlayer = event.getPlayer();

            if (clickType.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP
                    || clickType.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.SWAP) {

                final boolean isTotemInMainHand = player.getInventory().getItemInHand(InteractionHand.MAIN_HAND).getType().getName().toString().contains("TOTEM");
                final boolean isTotemInOffHand = player.getInventory().getItemInHand(InteractionHand.OFF_HAND).getType().getName().toString().contains("TOTEM");

                if (bukkitPlayer == null) {
                    return;
                }

                if (bukkitPlayer.getOpenInventory().getType() != InventoryType.PLAYER) {
                    return;
                }

                if (!isTotemInMainHand && !isTotemInOffHand) {
                    return;
                }

                ItemStack cursor = bukkitPlayer.getItemOnCursor();
                if (cursor.getType().toString().contains("TOTEM")) {
                    long currentTime = time();

                    if (bukkitPlayer.getOpenInventory().getType() != InventoryType.PLAYER) {
                        return;
                    }

                    if (!timeIntervals.isEmpty()) {
                        long timeInterval = currentTime - timeIntervals.get(timeIntervals.size() - 1);
                        timeIntervals.add(timeInterval);

                        if (timeIntervals.size() >= 3 && isPattern(timeIntervals)) {

                            if (flagAndAlert("(Pattern)\ntimes= " + Arrays.toString(timeIntervals.toArray()))) {
                                event.setCancelled(true);
                                closeInventory();
                                player.onPacketCancel();
                                player.getInventory().requiresRefresh = true;
                            }
                        }
                    }
                    timeIntervals.add(currentTime);
                }
            }
        }
    }

    private boolean isPattern(List<Long> timeIntervals) {
        long firstInterval = timeIntervals.get(0);
        for (int i = 1; i < timeIntervals.size(); i++) {
            if (Math.abs(timeIntervals.get(i) - firstInterval) > 100) {
                return false;
            }
        }
        return true;
    }
}
