package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "CrashK (Packet)", type = CheckType.CRASH, configName = "CrashK", description = "Check for big packets")
public final class CrashK extends Check implements PacketCheck {

    private int tabComplete;
    private int dig;
    private int place;
    private int windowClick;
    private int slot;
    private int attacks;

    public CrashK(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
            if (player.getCps() > 1000 && flagAndAlert("s= " + player.getCps()) && shouldModifyPackets()) {
                punish(event);
            } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
                if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    attacks++;
                    if (attacks > 100 && flagAndAlert("a= " + attacks) && shouldModifyPackets()) {
                        punish(event);
                    }
                }
            } else if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
                tabComplete++;
                if (tabComplete > 1000 && flagAndAlert("t= " + tabComplete) && shouldModifyPackets()) {
                    punish(event);
                } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                    dig++;
                    if (dig > 1000 && flagAndAlert("d= " + dig) && shouldModifyPackets()) {
                        punish(event);
                    } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                        place++;
                        if (place > 1000 && flagAndAlert("p= " + place) && shouldModifyPackets()) {
                            punish(event);
                        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
                            windowClick++;
                            if (windowClick > 50 && flagAndAlert("w= " + windowClick) && shouldModifyPackets()) {
                                punish(event);
                            } else if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
                                slot++;
                                if (slot > 1000 && flagAndAlert("s= " + slot) && shouldModifyPackets()) {
                                    punish(event);
                                }
                            } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
                                slot = 0;
                                windowClick = 0;
                                place = 0;
                                dig = 0;
                                tabComplete = 0;
                                attacks = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private void punish(PacketReceiveEvent event) {
        event.setCancelled(true);
        player.onPacketCancel();
        player.getSetbackTeleportUtil().executeViolationSetback();
        kickPlayer();
    }
}
