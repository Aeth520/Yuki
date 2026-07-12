package cn.aetheris.yuki.check.impl.combat.autoblock.prediction;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoBlock2 (InteractOrder)", type = CheckType.AUTOBLOCK, configName = "AutoBlock2", description = "Invalid interact order", decay = 0.64, experimental = true)
public final class AutoBlock2 extends Check implements PostPredictionCheck {

    private int invalid;
    private boolean interact;
    private boolean attack;

    public AutoBlock2(PlayerData player) {
        super(player);
        attack = false;
        interact = false;
        invalid = 0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (isExempt(ExemptType.CLIENT_ANTICHEAT, ExemptType.INVALID_GAMEMODE)) return;
            if (new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                attack = true;
            } else {
                interact = true;
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            if (attack && !interact) {
                if (!player.canSkipTicks()) {
                    if (flagAndAlert()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                        if (isAboveSetbackVl()) resetPlayerUseItem(player.bukkitPlayer);
                    }
                } else {
                    invalid++;
                }
            } else {
                rewardBufferAndVL();
            }
        }

        if (isTickPacket(event.getPacketType()) && !player.isCouldSkipTick()) {
            interact = attack = false;
            rewardVL();
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.isCouldSkipTick()) return;

        if (player.isTickingReliablyFor(3)) {
            if (buffer++ > 4) {
                for (; invalid >= 1; invalid--) {
                    if (flagAndAlert()) {
                        player.mitigateDamage();
                    }
                }
            }
        }

        invalid = 0;
        interact = attack = false;
        rewardVL();
    }
}
