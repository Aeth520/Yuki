package cn.aetheris.yuki.check.util.rotation;

import cn.aetheris.yuki.player.PlayerData;

public interface AimDetectionStrategy {
    void detect(PlayerData profile, DetectionContext context);

    String getCheckName();

    void changeTarget();

    void reset();
}