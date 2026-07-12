package cn.aetheris.yuki.check.impl.player.scaffold;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "ScaffoldG",
        configName = "ScaffoldG",
        decay = 0.86,
        description = "Invalid rotation like scaffold",
        experimental = true)
public final class ScaffoldG extends BlockPlaceCheck {

    private boolean isBridging;
    private int placed;
    private int lbPlaced;

    private boolean cancel;

    public ScaffoldG(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.isBlock()) {
            isBridging = isBridging(place);
            placed++;
            if (cancel) {
                place.setCancelled(true);
                cancel = false;
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!isExempt(ExemptType.TELEPORT, ExemptType.RESPAWN)) {
                isBridging = false;

            }
        }
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (!isBridging) {
            lbPlaced = 0;
            buffer = 0;
            return;
        }

        if (!player.isMoving()) {
            lbPlaced = 0;
            buffer = 0;
            return;
        }

        if ((Math.floor(player.getX()) != Math.floor(player.getLastX()) || Math.floor(player.getZ()) != Math.floor(player.getLastZ())
                && lbPlaced != placed)
        ) {
            if ((buffer += 2) > 4) {
                if (flagAndAlert("placed= " + placed)) {
                    if (violations > 5) cancel = true;
                    player.mitigateDamage();
                    rewardBufferAndVL();
                    if (violations > 5) player.mitigateDamage();
                }
            }
        } else if (placed - lbPlaced > 10 && buffer > 0) {
            rewardBufferAndVL();
        }
        lbPlaced = placed;
    }
}
