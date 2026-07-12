package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

@CheckData(name = "InventoryJ (Action)",
        configName = "InventoryJ",
        type = CheckType.INVENTORY,
        decay = 0.95)
public final class InventoryJ extends InventoryCheck {

    private int invalid;

    public InventoryJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow.WindowClickType clickType = new WrapperPlayClientClickWindow(event).getWindowClickType();

            if ((clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP || clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP_ALL) && player.packetActionProcessor.isQuickMoveClicking() || clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE && player.packetActionProcessor.isPickUpClicking()) {
                if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                    if (buffer++ > 5) {
                        if (flagAndAlert()) {
                            event.setCancelled(true);
                            closeInventory();
                            player.onPacketCancel();
                            player.getInventory().requiresRefresh = true;
                        }
                    } else {
                        invalid++;
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3) && !player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(0)) {
            for (; invalid >= 1; invalid--) {
                if (flagAndAlert()) {
                    player.mitigateDamage();
                }
            }
        }

        invalid = 0;
    }
}
