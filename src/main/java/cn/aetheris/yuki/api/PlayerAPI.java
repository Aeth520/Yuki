package cn.aetheris.yuki.api;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerAPI extends AntiCheatUser {

    @Nullable String getBukkitWorldName();

    @Nullable UUID getBukkitWorldUID();
}