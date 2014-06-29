package com.github.stephanenicolas.mimic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ })
@Retention(RetentionPolicy.CLASS)
/**
 * Defines that a field is to be copied. Can only be used inside {@link Mimic}.
 * @author SNI
 */
public @interface MimicField {
    /** Name of the field to mimic.*/
    String fieldName();
}
