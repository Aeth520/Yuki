package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

@CheckData(name = "CrashF", type = CheckType.CRASH, configName = "CrashF", description = "Invalid Slot")
public final class CrashF extends Check implements PacketCheck {

    public CrashF(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow click = new WrapperPlayClientClickWindow(event);

            final WrapperPlayClientClickWindow.WindowClickType clickType = click.getWindowClickType();
            final int button = click.getButton();
            final int windowId = click.getWindowId();
            final int slot = click.getSlot();

            if ((clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE || clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP) && windowId >= 0 && button < 0) {
                if (flagAndAlert("clickType=" + clickType + "\nbutton=" + button)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            } else if (windowId >= 0 && clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP && slot < 0) {
                if (flagAndAlert("clickType=" + clickType + "\nbutton=" + button + "\nslot=" + slot)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }

        }
    }

}
