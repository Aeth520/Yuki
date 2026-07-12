package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.player.exploit.ExploitA;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

@CheckData(name = "CrashE", type = CheckType.CRASH, configName = "CrashE", description = "Invalid ClientSettings")
public final class CrashE extends Check implements PacketCheck {

    public CrashE(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
            WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);

            int viewDistance = wrapper.getViewDistance();

            final ExploitA exploitA = player.checkManager.getCheck(ExploitA.class);

            boolean invalidLocale = exploitA.checkString(wrapper.getLocale());

            if (viewDistance < 2) {
                if (flagAndAlert("d= " + viewDistance) && shouldModifyPackets()) {
                    wrapper.setViewDistance(2);
                }
                if (invalidLocale) wrapper.setLocale("en_us");
            }
        }
    }
}
