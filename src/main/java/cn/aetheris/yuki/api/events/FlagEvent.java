package cn.aetheris.yuki.api.events;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.PlayerAPI;
import cn.aetheris.yuki.api.enums.CheckType;
import lombok.Getter;

/**
 * Fired when a check flags a violation.
 * Inherits {@link Reaction} from {@link YukiCancellableEvent} for granular control.
 */
@Getter
public class FlagEvent extends YukiCancellableEvent {
    private final PlayerAPI player;
    private final AbstractCheck check;

    public FlagEvent(PlayerAPI player, AbstractCheck check) {
        this.player = player;
        this.check = check;
    }

    public double getViolations() {
        return check.getViolations();
    }

    public CheckType getCheckType() {
        return check.getCheckType();
    }

    public boolean isSetback() {
        return check.getViolations() > check.getSetbackVL();
    }
}