package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import lombok.Getter;

/**
 * Fired when a punishment command is about to be executed.
 * Refactored to extend {@link YukiCancellableEvent} directly.
 */
@Getter
public class CommandExecuteEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;
    private final String command;

    public CommandExecuteEvent(PlayerAPI player, AbstractCheck check, String command) {
        this.player = player;
        this.check = check;
        this.command = command;
    }
}