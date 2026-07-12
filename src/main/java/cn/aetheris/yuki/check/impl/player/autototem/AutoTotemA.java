package cn.aetheris.yuki.check.impl.player.autototem;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import org.jetbrains.annotations.NotNull;

@CheckData(
        name = "AutoTotemA (Limit)",
        configName = "AutoTotemA",
        decay = 0.54,
        type = CheckType.AUTOTOTEM
)
public class AutoTotemA extends InventoryCheck {


    public AutoTotemA(@NotNull PlayerData player) {
        super(player);
    }
}
