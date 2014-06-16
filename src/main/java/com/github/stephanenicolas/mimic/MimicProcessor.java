package com.github.stephanenicolas.mimic;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.inject.Inject;

import com.github.drochetti.javassist.maven.ClassTransformer;

/**
 * Post processes all classes annotated with {@link Mimic}.
 * 
 * @author SNI
 *
 */
public class MimicProcessor extends ClassTransformer {

	@Inject
	private MimicCreator mimic;

	@Override
	protected boolean filter(CtClass candidateClass) throws Exception {
		// no support for non-static inner classes in javassist.
		if (candidateClass.getDeclaringClass() != null
				&& (candidateClass.getModifiers() & Modifier.STATIC) != 0) {
			return false;
		}
		return candidateClass.hasAnnotation(Mimic.class);
	}

	@Override
	protected void applyTransformations(final CtClass classToTransform)
			throws ClassNotFoundException, NotFoundException,
			CannotCompileException, MimicException {
		// Actually you must test if it exists, but it's just an example...
		getLogger().debug("Analysing " + classToTransform);

		Mimic mimicAnnnotation = (Mimic) classToTransform
				.getAnnotation(Mimic.class);
		Class<?> srcClass = mimicAnnnotation.sourceClass();

		CtClass src = ClassPool.getDefault().get(srcClass.getName());
		if (mimicAnnnotation.isMimicingInterfaces()
				&& mimicAnnnotation.isMimicingFields()
				&& mimicAnnnotation.isMimicingConstructors()
				&& mimicAnnnotation.isMimicingMethods()) {
			mimic.mimicClass(src, classToTransform);
		} else {
			if (mimicAnnnotation.isMimicingInterfaces()) {
				mimic.mimicInterfaces(src, classToTransform);
			}
			if (mimicAnnnotation.isMimicingFields()) {
				mimic.mimicFields(src, classToTransform);
			}
			if (mimicAnnnotation.isMimicingConstructors()) {
				mimic.mimicConstructors(src, classToTransform);
			}
			if (mimicAnnnotation.isMimicingMethods()) {
				mimic.mimicMethods(src, classToTransform);
			}
		}
		getLogger().debug(
				"Class " + classToTransform.getName() + " now mimics "
						+ src.getName());
	}
}
