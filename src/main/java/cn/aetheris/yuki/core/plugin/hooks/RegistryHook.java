package cn.aetheris.yuki.core.plugin.hooks;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.impl.misc.visual.manager.MetaDataManager;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.block.place.BlockPlaceResult;
import cn.aetheris.yuki.block.collision.CollisionData;
import cn.aetheris.yuki.block.collision.HitboxData;
import cn.aetheris.yuki.block.collision.RaycastData;
import cn.aetheris.yuki.block.collision.datatypes.OffsetCollisionBox;
import cn.aetheris.yuki.util.inventory.menu.MenuType;
import cn.aetheris.yuki.math.LegacyFastMath;
import cn.aetheris.yuki.math.OptifineFastMath;
import cn.aetheris.yuki.math.VanillaMath;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import cn.aetheris.yuki.protocol.nms.PaperUtils;

public final class RegistryHook extends AbstractHook {
    @Override
    public void hook() {
        CollisionData.register();
        HitboxData.register();
        OffsetCollisionBox.register();
        RaycastData.register();
        BlockPlaceResult.register();
        MetaDataManager.register();
        LegacyFastMath.register();
        OptifineFastMath.register();
        VanillaMath.register();
        PaperUtils.register();
        MenuType.register();
        Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), NMSUtils::register, 10L);
        super.enabled = true;
    }

    @Override
    public void unhook() {
        super.enabled = false;

    }
}
