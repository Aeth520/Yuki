package cn.aetheris.yuki.util.inventory.menu;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.Inventory;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

public final class NotImplementedMenu extends AbstractContainerMenu {
    public NotImplementedMenu(PlayerData player, Inventory playerInventory) {
        super(player, playerInventory);
        player.getInventory().isPacketInventoryActive = false;
        player.getInventory().requiresRefresh = true;
    }

    @Override
    public void doClick(int button, int slotID, WrapperPlayClientClickWindow.WindowClickType clickType) {

    }
}
