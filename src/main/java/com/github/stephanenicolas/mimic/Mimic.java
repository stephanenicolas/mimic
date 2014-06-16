package com.github.stephanenicolas.mimic;

/**
 * Can be used to mimic a class via post processing. On the target class, add
 * this annotation.
 * 
 * @author SNI
 *
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
}
