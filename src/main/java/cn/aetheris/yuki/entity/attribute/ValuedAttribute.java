package cn.aetheris.yuki.entity.attribute;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.latency.CompensatedEntities;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.protocol.attribute.Attribute;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ValuedAttribute {

    private static final Function<Double, Double> DEFAULT_GET_REWRITE = Function.identity();

    private final Attribute attribute;
    
    
    private final double min, max;
    private final double defaultValue;
    private WrapperPlayServerUpdateAttributes.Property lastProperty;
    private double value;

    
    
    private BiFunction<Double, Double, Double> setRewriter;
    private Function<Double, Double> getRewriter;

    private ValuedAttribute(Attribute attribute, double defaultValue, double min, double max) {
        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("Default value must be between min and max!");
        }

        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.getRewriter = DEFAULT_GET_REWRITE;
    }

    public static ValuedAttribute ranged(Attribute attribute, double defaultValue, double min,
                                         double max) {
        return new ValuedAttribute(attribute, defaultValue, min, max);
    }

    public ValuedAttribute withSetRewriter(BiFunction<Double, Double, Double> rewriteFunction) {
        this.setRewriter = rewriteFunction;
        return this;
    }

    
    public ValuedAttribute requiredVersion(PlayerData player, ClientVersion requiredVersion) {
        withSetRewriter((oldValue, newValue) -> {
            if (player.getClientVersion().isOlderThan(requiredVersion)) {
                return oldValue;
            }
            return newValue;
        });
        return this;
    }

    public ValuedAttribute withGetRewriter(Function<Double, Double> getRewriteFunction) {
        this.getRewriter = getRewriteFunction;
        return this;
    }

    public Attribute attribute() {
        return attribute;
    }

    public void reset() {
        this.value = defaultValue;
        this.lastProperty =
                null; 
    }

    public double get() {
        return getRewriter.apply(this.value);
    }

    public void override(double value) {
        this.value = value;
    }

    public Optional<WrapperPlayServerUpdateAttributes.Property> property() {
        return Optional.ofNullable(lastProperty);
    }

    public void recalculate() {
        with(lastProperty);
    }

    public double with(WrapperPlayServerUpdateAttributes.Property property) {
        double baseValue = property.getValue();
        double additionSum = 0;
        double multiplyBaseSum = 0;
        double multiplyTotalProduct = 1.0;

        List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers =
                property.getModifiers();
        modifiers.removeIf(modifier -> modifier.getUUID().equals(CompensatedEntities.SPRINTING_MODIFIER_UUID) ||
                modifier.getName().getKey().equals("sprinting"));

        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : modifiers) {
            switch (modifier.getOperation()) {
                case ADDITION:
                    additionSum += modifier.getAmount();
                    break;
                case MULTIPLY_BASE:
                    multiplyBaseSum += modifier.getAmount();
                    break;
                case MULTIPLY_TOTAL:
                    multiplyTotalProduct *= (1.0 + modifier.getAmount());
                    break;
            }
        }

        double newValue =
                MathUtil.clamp((baseValue + additionSum) * (1 + multiplyBaseSum) * multiplyTotalProduct,
                        min, max);
        if (setRewriter != null) {
            newValue = setRewriter.apply(this.value, newValue);
        }

        if (newValue < min || newValue > max) {
            throw new IllegalArgumentException("New value must be between min and max!");
        }

        this.lastProperty = property;
        return this.value = newValue;
    }
}