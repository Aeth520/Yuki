package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;

@CheckData(name = "PingSpoofD (Invalid)",
        configName = "PingSpoofD",
        description = "Invalid KeepAlive Sent With Magic",
        type = CheckType.PINGSPOOF)
public final class PingSpoofD extends Check implements PacketCheck {

    final Deque<Long> ids = new LinkedList<>();
    private Long next;

    public PingSpoofD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_10) || !shouldModifyPackets()) return;

        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            WrapperPlayServerKeepAlive ka = new WrapperPlayServerKeepAlive(event);
            this.ids.add(ka.getId());
            final String encoded = Long.toString(ka.getId());
            HookInit.getPacketEventsHook().sendPacket(player.getUser(), new WrapperPlayServerResourcePackSend("level://" + Math.random() + "/resources.zip", encoded, false, null));
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_10)
                || !shouldModifyPackets()) return;

        boolean exempt = isExempt(ExemptType.JOIN);

        if (event.getPacketType() == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            final WrapperPlayClientResourcePackStatus status = new WrapperPlayClientResourcePackStatus(event);

            
            
            
            
            

            

            if (!status.getResult().equals(WrapperPlayClientResourcePackStatus.Result.FAILED_DOWNLOAD) && !exempt) {
                if (flagAndAlert("Invalid Download Packet")) {
                    event.setCancelled(true);
                    player.mitigateDamage();
                    return;
                }
            }

            if (ids.isEmpty() && !exempt) {
                if (flagAndAlert("Packet coming from idk where")) {
                    event.setCancelled(true);
                    player.mitigateDamage();
                    return;
                }
            }

            final String hash = status.getHash();
            if (hash == null || hash.isEmpty()) {
                return;
            }

            long var = Long.decode(hash);

            if (!ids.contains(var) && !exempt) {
                if (flagAndAlert("Keep alive does not exist bruh")) {
                    event.setCancelled(true);
                    player.mitigateDamage();
                    return;
                }
            }


            Long id = ids.poll();

            if (id == null) {
                if (!exempt) {
                    if (alert("No ID found in the queue")) {
                        player.mitigateDamage();
                    }
                }
                return;
            }


            if (id != var && ids.contains(var)) {

                while (id != var && !ids.isEmpty()) {
                    id = ids.poll();

                    if (exempt)
                        continue;

                    if (flagAndAlert("Invalid identifier, polling queue...")) {
                        event.setCancelled(true);
                        player.mitigateDamage();
                    }
                }
            } else {
                next = var;
                ids.remove(var);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive ka = new WrapperPlayClientKeepAlive(event);

            
            
            
            
            
            
            
            

            if (next != null && next == ka.getId()) {
            } else if (!exempt && next != null) {
                if (flagAndAlert("now= " + next + "\nshould= " + ka.getId())) {
                    player.mitigateDamage();
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
