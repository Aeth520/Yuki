package cn.aetheris.yuki.check.impl.misc.spam;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

import java.util.LinkedList;
import java.util.Queue;

@CheckData(name = "SpamB", configName = "SpamB", experimental = true, type = CheckType.SPAM)
public final class SpamB extends Check implements PacketCheck {

    public final Queue<Long> messageTimestamps = new LinkedList<>();
    public final Queue<String> messageHistory = new LinkedList<>();
    public boolean cancelEnabled;
    public int cancelVL;
    public int messageHistorySize;
    public int fastThreshold;
    public double similarityThreshold;

    public SpamB(PlayerData player) {
        super(player);
    }

    @Override
    public void reload() {
        super.reload();
        this.cancelEnabled = getConfig().getBooleanElse(getConfigName() + ".should-cancel", true);
        this.cancelVL = getConfig().getIntElse(getConfigName() + ".threshold.cancel-vl", 5);
        this.similarityThreshold = getConfig().getDoubleElse(getConfigName() + ".threshold.similarity", 0.8);
        this.fastThreshold = getConfig().getIntElse(getConfigName() + ".frequency-detection.fast-threshold", 3);
        this.messageHistorySize = getConfig().getIntElse(getConfigName() + ".history-settings.message-history", 5);
    }
}