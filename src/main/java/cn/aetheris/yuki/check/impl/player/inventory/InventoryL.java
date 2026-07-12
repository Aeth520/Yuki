package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;

@CheckData(name = "InventoryL",
        configName = "InventoryL",
        type = CheckType.INVENTORY,
        decay = 0.6)
public final class InventoryL extends InventoryCheck {

    private int containerType = -1;
    private int containerId = -1;

    public InventoryL(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow window = new WrapperPlayServerOpenWindow(event);
            this.containerType = window.getType();
            this.containerId = window.getContainerId();
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            WrapperPlayClientClickWindow.WindowClickType clickType = wrapper.getWindowClickType();
            int button = wrapper.getButton();

            
            boolean flag = switch (clickType) {
                case PICKUP, QUICK_MOVE, CLONE -> button > 2 || button < 0;
                case SWAP -> (button > 8 || button < 0) && button != 40;
                case THROW -> button != 0 && button != 1;
                case QUICK_CRAFT -> button == 3 || button == 7 || button > 10 || button < 0;
                case PICKUP_ALL -> button != 0;
                case UNKNOWN -> true;
            };

            if (flag) {
                if (buffer++ > 3) {
                    if (flagAndAlert("type= " + clickType.toString().toLowerCase() + "\nbutton= " + button + (wrapper.getWindowId() == containerId ? "\ncontainer= " + containerType : "")) && shouldModifyPackets()) {
                        event.setCancelled(true);
                        closeInventory();
                        player.onPacketCancel();
                    }
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}