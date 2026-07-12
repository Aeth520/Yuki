package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.command.MainCommand;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;

import static cn.aetheris.yuki.util.command.CommandBuilder.registerCommand;

public final class CommandInit implements Init {

    @Override
    public void init() {
        
        registerCommand(Yuki.getInstance(), new MainCommand(), "主命令", "yuki", "gr");

        LogUtils.console("&3Yuki &8» &aCommands Initialized!");
    }
}