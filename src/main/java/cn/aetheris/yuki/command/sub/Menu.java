package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Menu extends AbstractCommand {

    public Menu() {
        super(
                "Open menu",
                "yuki.commands.menu",
                true);
    }

    @Override
    public void execute(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        new cn.aetheris.yuki.menu.Menu(sender).open(sender);
    }
}