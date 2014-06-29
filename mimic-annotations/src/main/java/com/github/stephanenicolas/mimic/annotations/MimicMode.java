package com.github.stephanenicolas.mimic.annotations;

public enum MimicMode {
    /** Will inject code at beginning of target method.*/
    AT_BEGINNING,
    /** Will inject code at the end of target method.*/
    BEFORE_RETURN,
    /** Will inject code before a call to an overriden method.*/
    BEFORE_SUPER,
    /** Will inject code after a call to an overriden method.*/
    AFTER_SUPER,
    /** Will inject code instead of a call to an overriden method.*/
    REPLACE_SUPER,
    /** Will inject code before a call to a given method.*/
    BEFORE,
    /** Will inject code after a call to a given method.*/
    AFTER;
}
