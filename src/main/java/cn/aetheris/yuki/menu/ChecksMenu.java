package cn.aetheris.yuki.menu;

import cn.aetheris.yuki.menu.check.CombatMenu;
import cn.aetheris.yuki.menu.check.MiscMenu;
import cn.aetheris.yuki.menu.check.MoveMenu;
import cn.aetheris.yuki.menu.check.PlayerMenu;
import cn.aetheris.yuki.util.message.ColorUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ChecksMenu extends FastInv {
    public ChecksMenu() {
        super(45, ColorUtils.color("&3Yuki &7- &eChecks"));
        int[] blue = new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43};
        for (int slot : blue) {
            this.setItem(slot, MenuUtil.getBlueFrame());
        }
        int[] white = new int[]{0, 2, 4, 6, 8, 10, 16, 18, 20, 22, 24, 26, 28, 34, 36, 38, 42, 44};
        for (int slot : white) {
            this.setItem(slot, MenuUtil.getWhiteFrame());
        }
        this.setItem(40, MenuUtil.getBack(), e -> new Menu((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));
        this.setItem(12, new ItemBuilder(Material.BOOK).name(ColorUtils.color("&bCombat")).build(), e -> new CombatMenu().open((Player) e.getWhoClicked()));
        this.setItem(14, new ItemBuilder(Material.BOOK).name(ColorUtils.color("&bMisc")).build(), e -> new MiscMenu().open((Player) e.getWhoClicked()));
        this.setItem(30, new ItemBuilder(Material.BOOK).name(ColorUtils.color("&bMovement")).build(), e -> new MoveMenu().open((Player) e.getWhoClicked()));
        this.setItem(32, new ItemBuilder(Material.BOOK).name(ColorUtils.color("&bPlayer")).build(), e -> new PlayerMenu().open((Player) e.getWhoClicked()));
    }

    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
