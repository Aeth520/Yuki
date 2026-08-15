package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.math.stats.ChiSquare;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

/**
 * Chi-square click pattern analysis.
 *
 * <p>Builds a contingency table of consecutive click-interval pairs:
 * rows = previous interval bucket, columns = current interval bucket.
 * Human clicking shows strong autocorrelation (previous interval predicts the
 * next). Legal clients and autoclickers break this independence structure
 * differently — a very low chi-square p-value means the observed transition
 * pattern is statistically implausible for human input.</p>
 */
@CheckData(name = "AutoClickerR (ChiSquare)", description = "Click interval transitions are statistically implausible", type = CheckType.AUTOCLICKER, configName = "AutoClickerR", decay = 0.25, experimental = true)
public final class AutoClickerR extends Check implements PacketCheck {

    // click interval buckets in ms: <50, 50-80, 80-120, 120-200, >200
    private static final int BUCKETS = 5;
    private static final int[] INTERVAL_BOUNDS = {50, 80, 120, 200};

    private final ChiSquare transitions = new ChiSquare(BUCKETS, BUCKETS);
    private long lastSwing = -1;
    private int prevBucket = -1;
    private int sampleCount;

    private int minSamples;
    private double maxPValue;

    public AutoClickerR(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) {
            return;
        }

        if (isExempt(ExemptType.LAGGING)) {
            return;
        }

        long now = time();
        if (lastSwing > 0) {
            long interval = now - lastSwing;

            if (interval > 0 && interval < 1000) {
                int bucket = bucketOf(interval);

                if (prevBucket >= 0) {
                    transitions.increment(prevBucket, bucket);
                    sampleCount++;

                    if (sampleCount >= minSamples && sampleCount % 20 == 0) {
                        double p = transitions.pValue();
                        // p-value very low => pattern implausible under independence
                        if (p > 0 && p < maxPValue) {
                            if (flagAndAlert("p= " + String.format("%.2E", p)
                                    + " samples= " + sampleCount
                                    + " chi2= " + String.format("%.1f", transitions.chi2()))) {
                                // rebuild table after a flag to avoid re-flagging identical data
                                transitions.clear();
                                sampleCount = 0;
                            }
                        }
                    }
                }
                prevBucket = bucket;
            } else {
                // too long gap: reset the transition chain
                prevBucket = -1;
            }
        }
        lastSwing = now;
    }

    private static int bucketOf(long intervalMs) {
        for (int i = 0; i < INTERVAL_BOUNDS.length; i++) {
            if (intervalMs < INTERVAL_BOUNDS[i]) {
                return i;
            }
        }
        return INTERVAL_BOUNDS.length;
    }

    @Override
    public void reload() {
        super.reload();
        minSamples = getConfig().getIntElse(getConfigName() + ".min-samples", 100);
        maxPValue = getConfig().getDoubleElse(getConfigName() + ".max-p-value", 1.0E-4);
    }
}
