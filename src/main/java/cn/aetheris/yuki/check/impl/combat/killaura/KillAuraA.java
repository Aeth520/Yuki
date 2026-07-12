package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "KillAuraA (Action)", type = CheckType.KILLAURA, configName = "KillAuraA", description = "Invalid Entity Action", decay = 0.25)
public final class KillAuraA extends Check implements PacketCheck {

    private final boolean supportVersion;
    private boolean sent;

    public KillAuraA(PlayerData player) {
        super(player);
        supportVersion = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            boolean attacking = interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
            if (attacking && sent && supportVersion) {
                if (flagAndAlert()) {
                    player.mitigateDamage();
                }
            } else {
                rewardVL();
            }
        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction entityAction = new WrapperPlayClientEntityAction(event);
            switch (entityAction.getAction()) {
                case START_SPRINTING, STOP_SPRINTING, START_SNEAKING, STOP_SNEAKING -> sent = true;
            }
        } else if (isTickPacket(event.getPacketType())) {
            sent = false;
        }
    }
}
