package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;

import java.util.StringJoiner;

@CheckData(name = "ImpossibleC (Abilities)", configName = "ImpossibleC", description = "Spoofed Abilities", type = CheckType.IMPOSSIBLE)
public final class ImpossibleC extends Check implements PacketCheck {
    public ImpossibleC(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES) {
            WrapperPlayClientPlayerAbilities abilities = new WrapperPlayClientPlayerAbilities(event);

            boolean fly = abilities.isFlying() && !player.isFlying;

            boolean creative = abilities.isInCreativeMode().isPresent()
                    && abilities.isInCreativeMode().get()
                    && !player.gamemode.equals(GameMode.CREATIVE);
            boolean groundSpeed = player.bukkitPlayer != null && abilities.getWalkSpeed().isPresent()
                    && abilities.getWalkSpeed().get() > (player.bukkitPlayer.getWalkSpeed() / 2.F);

            flag:
            {
                boolean flag = fly || creative || groundSpeed;

                if (!flag) break flag;

                StringJoiner joiner = new StringJoiner(" ");

                if (fly) joiner.add("Spoofed Flying");
                if (creative) joiner.add("Spoofed Creative");
                if (groundSpeed) joiner.add("Spoofed GroundSpeed");

                if (buffer++ > 1) {
                    if (flagAndAlert("t= " + joiner) && shouldModifyPackets())
                        player.onPacketCancel();
                    event.setCancelled(true);
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}


