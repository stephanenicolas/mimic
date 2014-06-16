package com.github.stephanenicolas.mimic;

/**
 * Can be used to mimic a class via post processing.
 * On the target class, add this annotation.
 * @author SNI
 *
 */
public @interface Mimic {
	//http://stackoverflow.com/a/7070325/693752
	Class<?> sourceClass();
}
