package cn.aetheris.yuki.check;

import cn.aetheris.yuki.api.enums.CheckType;

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

}
