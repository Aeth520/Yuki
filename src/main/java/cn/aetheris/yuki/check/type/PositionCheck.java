package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.util.update.PositionUpdate;

public interface PositionCheck extends AbstractCheck {

    default void onPositionUpdate(final PositionUpdate positionUpdate) {
    }
}
