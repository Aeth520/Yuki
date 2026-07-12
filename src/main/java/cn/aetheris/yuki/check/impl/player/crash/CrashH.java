package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

@CheckData(name = "CrashH",
        type = CheckType.CRASH,
        configName = "CrashH",
        description = "Invalid TabComplete")
public final class CrashH extends Check implements PacketCheck {

    public CrashH(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);

            String text = wrapper.getText();

            final int length = text.length();

            if (length > 256) {
                if (shouldModifyPackets()) {
                    alert("l= " + length);
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
                if (buffer++ > 5) {
                    if (flagAndAlert("(text)\nl= " + length + "\nt= " + text) && shouldModifyPackets()) {
                        buffer = 0;
                        return;
                    }
                } else {
                    rewardBufferAndVL();
                }
            }


            int index;
            if (length > 64 && ((index = text.indexOf(' ')) == -1 || index >= 64)) {
                if (shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
                if (buffer++ > 4)
                    if (flagAndAlert("(invalid)\nl= " + length)) {
                        buffer = 0;
                        return;
                    }
                rewardVL();
            } else {
                rewardBufferAndVL();
            }
        }
    }
}