package com.github.stephanenicolas.mimic.annotations;

import com.github.stephanenicolas.mimic.MimicMode;

/** Defines how to mimic methods.*/
public @interface MimicMethod {
    
    /** The name of the method to mimic. Due to an issue in javassist (#9 on github), one of the overloads will be picked here.*/
    String methodName();

    /** The mode of mimicing.*/
    MimicMode mode() default MimicMode.AFTER_SUPER;
}
