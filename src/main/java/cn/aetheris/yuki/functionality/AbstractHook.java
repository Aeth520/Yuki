package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.core.plugin.interfaces.Hook;
import lombok.Getter;

@Getter
public abstract class AbstractHook implements Hook {
    public boolean enabled = false;
}
