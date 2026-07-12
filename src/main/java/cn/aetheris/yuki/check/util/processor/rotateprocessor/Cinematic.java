package cn.aetheris.yuki.check.util.processor.rotateprocessor;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.player.HeadRotation;
import cn.aetheris.yuki.util.graphing.GraphUtil;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Cinematic extends Check implements RotationCheck, PacketCheck {

    private static final double CINEMATIC_CONSTANT = 0.0078125F;
    private final List<Double> yawSamples = new ArrayList<>(),
            pitchSamples = new ArrayList<>();
    private int isTotallyNotCinematic = 0;
    private long lastSmooth = 0L, lastHighRate = 0L;
    private int lastCinematicTicks;
    private int cinematicTicks;
    private int tick;

    public Cinematic(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            tick++;
        } else {
            tick = 0;
        }
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        final HeadRotation from = rotationUpdate.getFrom();
        final HeadRotation to = rotationUpdate.getTo();
        final long now = time();

        final float deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        final float deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        final float lastDeltaYaw = rotationUpdate.getProcessor().getLastLastYaw();
        final float lastDeltaPitch = rotationUpdate.getProcessor().getLastLastPitch();
        final double differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final double differencePitch = Math.abs(deltaPitch - lastDeltaPitch);
        final double joltYaw = Math.abs(differenceYaw - deltaYaw);
        final double joltPitch = Math.abs(differencePitch - deltaPitch);

        final boolean cinematic2 = (now - this.lastHighRate > 250L || now - this.lastSmooth < 9000L);
        if (joltYaw > 1.0D && joltPitch > 1.0D) {
            lastHighRate = now;
        }

        if (deltaYaw > 0.0F && deltaPitch > 0.0F) {
            this.yawSamples.add((double) deltaYaw);
            this.pitchSamples.add((double) deltaPitch);
            if (this.yawSamples.size() >= 20 && this.pitchSamples.size() >= 20) {
                Set<Double> shannonYaw = new HashSet<>();
                Set<Double> shannonPitch = new HashSet<>();
                List<Double> stackYaw = new ArrayList<>();
                List<Double> stackPitch = new ArrayList<>();
                for (Double yawSample : this.yawSamples) {
                    stackYaw.add(yawSample);
                    stackPitch.add(yawSample);
                    if (stackYaw.size() >= 10 && stackPitch.size() >= 10) {
                        shannonYaw.add(MathUtil.getShannonEntropy(stackYaw));
                        shannonPitch.add(MathUtil.getShannonEntropy(stackPitch));
                        stackYaw.clear();
                        stackPitch.clear();
                    }
                }
                
                if (shannonYaw.size() != 1 || shannonPitch.size() != 1 ||
                        !shannonYaw.iterator().next().equals(shannonPitch.iterator().next())) {
                    this.isTotallyNotCinematic = 20;
                }
                GraphUtil.GraphResult resultsYaw = GraphUtil.getGraph(this.yawSamples);
                GraphUtil.GraphResult resultsPitch = GraphUtil.getGraph(this.pitchSamples);
                if (resultsYaw.positives() > resultsYaw.negatives() || resultsPitch.positives() > resultsPitch.negatives()) {
                    this.lastSmooth = now;
                }
                this.yawSamples.clear();
                this.pitchSamples.clear();
            }
            updateCinematic2(rotationUpdate, cinematic2);
        }
        updateCinematic(rotationUpdate);
    }

    
    private void updateCinematic2(RotationUpdate rotationUpdate, boolean cinematic2Flag) {
        boolean allowed = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.allowed-cinematic", false);
        if (this.isTotallyNotCinematic > 0 || !allowed) {
            this.isTotallyNotCinematic--;
            rotationUpdate.setCinematic2(false);
        } else {
            rotationUpdate.setCinematic2(cinematic2Flag);
        }
    }

    
    private void updateCinematic(RotationUpdate rotationUpdate) {

        float deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();
        float deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();

        if (deltaPitch == 0F || deltaYaw == 0F) {
            return;
        }

        float yawAccel = rotationUpdate.getProcessor().getYawAccel();
        float pitchAccel = rotationUpdate.getProcessor().getPitchAccel();
        float lastDeltaYaw = rotationUpdate.getProcessor().getLastDeltaYaw();
        float lastDeltaPitch = rotationUpdate.getProcessor().getLastDeltaPitch();

        boolean invalid = MathUtil.isExponentiallySmall(yawAccel)
                || yawAccel == 0F
                || MathUtil.isExponentiallySmall(pitchAccel)
                || pitchAccel == 0F;

        long expandedDeltaYaw = (long) (deltaYaw * MathUtil.EXPANDER);
        long expandedLastDeltaYaw = (long) (lastDeltaYaw * MathUtil.EXPANDER);
        long expandedDeltaPitch = (long) (deltaPitch * MathUtil.EXPANDER);
        long expandedLastDeltaPitch = (long) (lastDeltaPitch * MathUtil.EXPANDER);
        double constantYaw = MathUtil.getGcd(expandedDeltaYaw, expandedLastDeltaYaw);
        double constantPitch = MathUtil.getGcd(expandedDeltaPitch, expandedLastDeltaPitch);

        boolean cinematic = !invalid && yawAccel < 1F && pitchAccel < 1F;
        if (cinematic) {
            if (constantYaw < CINEMATIC_CONSTANT && constantPitch < CINEMATIC_CONSTANT) {
                cinematicTicks++;
            }
        } else {
            cinematicTicks = Math.max(cinematicTicks - 1, 0);
        }
        
        if (cinematicTicks > 5) {
            cinematicTicks--;
        }

        boolean allowed = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.allowed-cinematic", false);
        rotationUpdate.setCinematic((cinematicTicks > 2 || tick - lastCinematicTicks < 80) && allowed);

        if (rotationUpdate.isCinematic() && cinematicTicks > 3) {
            lastCinematicTicks = tick;
        }
    }
}
