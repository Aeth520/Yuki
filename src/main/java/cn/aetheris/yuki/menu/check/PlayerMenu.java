package cn.aetheris.yuki.menu.check;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.menu.ChecksMenu;
import cn.aetheris.yuki.menu.MenuUtil;
import cn.aetheris.yuki.util.message.ColorUtils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerMenu extends FastInv {
    public PlayerMenu() {
        super(45, ColorUtils.color("&3Yuki &7- &bPlayer"));
        MenuUtil.setCheckItems(this, CheckType.PLAYER);
        this.setItem(44, MenuUtil.getBack(), e -> new ChecksMenu().open((Player) e.getWhoClicked()));
    }

    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
