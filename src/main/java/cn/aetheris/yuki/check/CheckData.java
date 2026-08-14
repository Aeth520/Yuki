package cn.aetheris.yuki.check;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.enums.MitigationStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckData {
    String name() default "UNKNOWN";

    String alternativeName() default "UNKNOWN";

    String configName() default "DEFAULT";

    String description() default "NONE";

    CheckType type() default CheckType.NONE;

    double decay() default 0.35;

    double setback() default 40;

    boolean experimental() default false;

    CheckPipeline pipeline() default CheckPipeline.DEFAULT;

    /**
     * Marks this check as a utility / infrastructure class rather than a real detection.
     * Utility classes are excluded from permission registration, GUI listings, and violation tracking.
     */
    boolean utilityClass() default false;

    /**
     * Per-check mitigation strategy. Controls how aggressively this check responds to violations.
     * @see MitigationStrategy
     */
    MitigationStrategy mitigation() default MitigationStrategy.CAREFUL;
}