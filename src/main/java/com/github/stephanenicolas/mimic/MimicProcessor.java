package com.github.stephanenicolas.mimic;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import com.github.drochetti.javassist.maven.ClassTransformer;

public class MimicProcessor extends ClassTransformer {

	private MimicCreator mimic = new MimicCreator();

	//for tests
	protected void setMimic(MimicCreator mimic) {
		this.mimic = mimic;
	}
	
	@Override
	protected boolean filter(CtClass candidateClass) throws Exception {
		//no support for non-static inner classes in javassist.
		if( candidateClass.getDeclaringClass() != null && (candidateClass.getModifiers() & Modifier.STATIC) != 0 ) {
			return false;
		}
		return candidateClass.hasAnnotation(Mimic.class);
	}

	@Override
	protected void applyTransformations(final CtClass classToTransform) throws ClassNotFoundException, NotFoundException, CannotCompileException, MimicException {
		// Actually you must test if it exists, but it's just an example...
		getLogger().debug("Analysing " + classToTransform);

		Mimic mimicAnnnotation = (Mimic)classToTransform.getAnnotation(Mimic.class);
		Class<?> srcClass = mimicAnnnotation.sourceClass();

		CtClass src = ClassPool.getDefault().get(srcClass.getName());
		mimic.mimicClass(src, classToTransform);
		getLogger().debug("Class " + classToTransform.getName() + " now mimics " + src.getName());
	}
}
