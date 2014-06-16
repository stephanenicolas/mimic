package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class MimicProcessorTest {

	private MimicProcessor mimicProcessor;
	private CtClass src;
	private CtClass dst;
	
	@Before
	public void setUp() {
		mimicProcessor = new MimicProcessor();
		src = ClassPool.getDefault().makeClass("Src"+TestCounter.testCounter);
		dst = ClassPool.getDefault().makeClass("Dst"+TestCounter.testCounter);
		TestCounter.testCounter++;
	}
	
	@Test
	public void testFilter_does_filter_correctly_a_mimic_annotation() throws Exception {
		addMimicAnnotation(dst, src.getName());
		
		//WHEN
		boolean filter = mimicProcessor.filter(dst);

		// THEN
		assertTrue(filter);
	}
	
	@Test
	public void testFilter_does_filter_correctly_a_no_mimic_annotation() throws Exception {
		// GIVEN
		
		//WHEN
		boolean filter = mimicProcessor.filter(dst);

		// THEN
		assertFalse(filter);
	}
	
	@Test
	public void testTransform() throws Exception {
		// GIVEN
		addMimicAnnotation(dst, TestSourceClass.class.getName());
		MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
		mimicProcessor.setMimic(mimicMock);
		mimicMock.mimicClass(EasyMock.eq(ClassPool.getDefault().get(TestSourceClass.class.getName())), EasyMock.eq(dst));
		EasyMock.replay(mimicMock);
		
		//WHEN
		mimicProcessor.applyTransformations(dst);

		// THEN
		EasyMock.verify(mimicMock);
	}

	private void addMimicAnnotation(CtClass dst, String sourceClassName) {
		ClassFile cf = dst.getClassFile();
		ConstPool cp = cf.getConstPool();
		AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);

		Annotation a = new Annotation(Mimic.class.getName(),cp);
		a.addMemberValue("sourceClass", new ClassMemberValue(sourceClassName, cp));
		attr.setAnnotation(a);
		cf.addAttribute(attr);
		cf.setVersionToJava5();
	}
}
