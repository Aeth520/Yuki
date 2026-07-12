package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class CommandExecuteEvent extends FlagEvent {
    private static final HandlerList handlers = new HandlerList();

    private final AbstractCheck check;
    private final String command;

    public CommandExecuteEvent(PlayerAPI player, AbstractCheck check, String command) {
        super(player, check); 
        this.check = check;
        this.command = command;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}