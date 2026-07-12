package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;

import java.util.HashSet;
import java.util.Set;

@CheckData(name = "CrashM (PayLoad)", type = CheckType.CRASH, configName = "CrashM", description = "Invalid Payload Book")
public final class CrashM extends Check implements PacketCheck {

    private static final Set<ItemType> book = new HashSet<>();

    static {
        book.add(ItemTypes.WRITTEN_BOOK);
        book.add(ItemTypes.ENCHANTED_BOOK);
        book.add(ItemTypes.BOOK);
    }

    public CrashM(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage payload = new WrapperPlayClientPluginMessage(event);
            String channel = payload.getChannelName();
            if (channel.equals("MC|BOpen") || channel.equals("MC|BEdit") || channel.equals("MC|BSign")) {
                ItemStack mian = player.getInventory().getItemInHand(InteractionHand.MAIN_HAND);
                if (mian != null && book.contains(mian.getType())) {
                    if (flagAndAlert("(MAIN)")) {
                        event.setCancelled(true);
                        player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                        kickPlayer();
                    }
                }
                ItemStack off = player.getInventory().getItemInHand(InteractionHand.OFF_HAND);
                if (off != null && book.contains(off.getType())) {
                    if (flagAndAlert("(OFF)")) {
                        event.setCancelled(true);
                        player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                        kickPlayer();
                    }
                }
            }
        }
    }
}
