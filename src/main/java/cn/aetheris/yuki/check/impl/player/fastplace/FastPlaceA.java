package cn.aetheris.yuki.check.impl.player.fastplace;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockPlaceCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;

import java.util.HashMap;
import java.util.Map;

@CheckData(name = "FastPlaceA", configName = "FastPlaceA", decay = 0.55, type = CheckType.FASTPLACE)
public class FastPlaceA extends BlockPlaceCheck {

    private final Map<Integer, Long> placed;
    private int places;
    private int limit;
    private long needTime;

    public FastPlaceA(PlayerData player) {
        super(player);
        placed = new HashMap<>();
    }

    @Override
    public void onBlockPlace(BlockPlace place) {
        if (!place.isBlock()) {
            places = 0;
            buffer *= 0.85;
        }

        if (place.isCancelled()) {
            buffer = 0;
            return;
        }

        if (place.getMaterial().getName().contains("HOE")) {
            buffer *= 0.95;
            return;
        }

        if (place.getMaterial().getName().contains("SCAFFOLD")) {
            buffer *= 0.95;
            return;
        }

        if (isExempt(ExemptType.VEHICLE, ExemptType.VEHICLE_SWITCH)) {
            places = 0;
            return;
        }

        if (player.isDigging()) {
            return;
        }

        ++places;

        placed.put(places, time());

        if (time() - placed.get(1) > needTime) {
            if (placed.size() > limit) {
                if (buffer++ > 5) {
                    if (flagAndAlert("placed= " + placed.size())) {
                        place.setCancelled(true);
                        player.mitigateDamage();
                    }
                }
            }
            placed.clear();
            places = 0;
        }
    }

    @Override
    public void reload() {
        super.reload();
        limit = getConfig().getIntElse(getConfigName() + ".limit", 20);
        needTime = getConfig().getLongElse(getConfigName() + ".need-time", 1000L);
    }
}
