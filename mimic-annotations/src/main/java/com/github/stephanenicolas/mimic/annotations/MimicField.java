package com.github.stephanenicolas.mimic.annotations;

import java.lang.annotation.Target;

@Target({ })
/**
 * Defines that a field is to be copied. Can only be used inside {@link Mimic}.
 * @author SNI
 */
public @interface MimicField {
    String fieldName();
}
