package cn.aetheris.yuki.check.type;

import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.util.update.RotationUpdate;

public interface RotationCheck extends AbstractCheck {

    default void process(final RotationUpdate rotationUpdate) {
    }
}
