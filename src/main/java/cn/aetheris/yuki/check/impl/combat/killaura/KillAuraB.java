package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "KillAuraB (Multi)", type = CheckType.KILLAURA, configName = "KillAuraB", description = "Multi Aura", decay = 0.65)
public final class KillAuraB extends Check implements PacketCheck {

    private int targetAmount;
    private int lastEntity;
    private int cps;

    public KillAuraB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final boolean isSentPing = isTransaction(event.getPacketType());
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactType = new WrapperPlayClientInteractEntity(event);

            cps = player.getCps();

            boolean attacking = interactType.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;

            int currentTarget = interactType.getEntityId();

            if (currentTarget != lastEntity && attacking) {
                targetAmount++;
            }

            lastEntity = currentTarget;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            reset();
            return;
        } else if (isExempt(ExemptType.TELEPORT, ExemptType.MYTHIC_ITEM_ATTACK)) {
            reset();
            return;
        }

        if (targetAmount > 1) {
            if (buffer++ > 3) {
                if (!isExempt(ExemptType.CLIENT_VERSION)) {
                    if (flagAndAlert("(Normal)\nt= " + targetAmount + "\nc= " + cps)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                        player.mitigateDamage();
                    }
                }
                reset();
            } else if (isSentPing && isExempt(ExemptType.CLIENT_VERSION)) {
                if (this.targetAmount > 1) {
                    if (buffer++ > 2) {
                        if (flagAndAlert("(1.9+)\nt= " + targetAmount + "\nc= " + cps)) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            player.mitigateDamage();
                        }
                    }
                }
                reset();
            }
        }
    }

    private void reset() {
        targetAmount = 0;
        rewardBufferAndVL();
    }
}