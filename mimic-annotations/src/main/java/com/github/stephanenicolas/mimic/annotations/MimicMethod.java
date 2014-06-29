package com.github.stephanenicolas.mimic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ })
@Retention(RetentionPolicy.CLASS)
/** Defines how to mimic methods. Can only be used inside {@link Mimic}. */
public @interface MimicMethod {

    /** The name of the method to mimic. Due to an issue in javassist (#9 on github), one of the overloads will be picked here.*/
    String methodName();

    /** The mode of mimicing. Defaults to {@link MimicMode.AFTER_SUPER}.*/
    MimicMode mode() default MimicMode.AFTER_SUPER;

    /** The method after or before which to inject mimiced code. Only used for {@link MimicMode#AFTER} and  {@link MimicMode#BEFORE}*/
    String insertionMethod() default "";
}
