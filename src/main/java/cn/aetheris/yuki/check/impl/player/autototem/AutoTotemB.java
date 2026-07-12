package cn.aetheris.yuki.check.impl.player.autototem;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CheckData(name = "AutoTotemB (Sort)",
        configName = "AutoTotemB",
        decay = 0.54,
        type = CheckType.AUTOTOTEM,
        experimental = true)
public class AutoTotemB extends InventoryCheck {

    private final List<Integer> switchSlots = new LinkedList<>();

    public AutoTotemB(@NotNull PlayerData player) {
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
                    int currentSlot = clickType.getSlot();
                    long currentTime = time();
                    long lastTotemSwitchTime = 0;
                    long diff = Math.abs(currentTime - lastTotemSwitchTime);


                    if (bukkitPlayer.getOpenInventory().getType() != InventoryType.PLAYER) {
                        return;
                    }

                    switchSlots.add(currentSlot);
                    if (switchSlots.size() >= 3) {
                        if (isMechanicalSwitch(switchSlots)) {
                            if (buffer++ > 5) {
                                if (flagAndAlert("(Auto)\ns= " + Arrays.toString(switchSlots.toArray()))) {
                                    event.setCancelled(true);
                                    closeInventory();
                                    player.onPacketCancel();
                                    player.getInventory().requiresRefresh = true;
                                }
                            } else {
                                rewardBufferAndVL();
                            }
                        }
                        switchSlots.clear();
                    }
                }

            }
        }
    }

    private boolean isMechanicalSwitch(List<Integer> slots) {
        for (int i = 0; i < slots.size() - 1; i++) {
            if (slots.get(i) + 1 != slots.get(i + 1)) {
                return false;
            }
        }
        return true;
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
