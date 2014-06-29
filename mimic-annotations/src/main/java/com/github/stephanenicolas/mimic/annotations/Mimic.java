package com.github.stephanenicolas.mimic.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
/**
 * Can be used to mimic a class via post processing. On the target class, add
 * this annotation.
 *
 * @author SNI
 */
public @interface Mimic {
    // http://stackoverflow.com/a/7070325/693752
    /** The class to mimic. */
    Class<?> sourceClass();

    /** Whether or not to mimic interfaces. Defaults to true. */
    boolean isMimicingInterfaces() default true;

    /** Whether or not to mimic fields. Defaults to true. */
    boolean isMimicingFields() default true;

    /** Whether or not to mimic constructors. Defaults to true. */
    boolean isMimicingConstructors() default true;

    /** Whether or not to mimic methods. Defaults to true. */
    boolean isMimicingMethods() default true;

    /** Defines which methods are copied, and how. If not defined, they are all copied, using default mimic mode.*/
    MimicMethod[] mimicMethods() default { };

    /** Default mimic mode for all methods. */
    MimicMode defaultMimicMode() default MimicMode.AFTER_SUPER;

    /** Defines which fields are copied, and how. If not defined, they are all copied.*/
    MimicField[] mimicFields() default { };

}
