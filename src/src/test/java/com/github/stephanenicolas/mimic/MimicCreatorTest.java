package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

import org.junit.Before;
import org.junit.Test;

/**
 * Can be used to mimic a class via post processing. On the target class, add
 * this annotation.
 * 
 * @author SNI
 *
 */
public class MimicCreatorTest {

	private MimicCreator mimicCreator;
	private CtClass src;
	private CtClass dst;
	
	@Before
	public void setUp() {
		mimicCreator = new MimicCreator();
		src = ClassPool.getDefault().makeClass("Src"+TestCounter.testCounter);
		dst = ClassPool.getDefault().makeClass("Dst"+TestCounter.testCounter);
		TestCounter.testCounter++;
	}
	
	@Test
	public void testMimicInterfaces() throws CannotCompileException, MimicException, NotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		// GIVEN
		CtClass interfazz = ClassPool.getDefault().makeInterface("Able"+TestCounter.testCounter);
		src.addInterface(interfazz);
		
		//WHEN
		mimicCreator.mimicInterfaces(src, dst);

		// THEN
		CtClass fooInterface = dst.getInterfaces()[0];
		assertNotNull(fooInterface);
		Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);
		Class<?> realInterface = ClassPool.getDefault().toClass(dst).getInterfaces()[0];
		assertEquals(realInterface, interfaceClass);
	}
	
	@Test
	public void testMimicFields() throws CannotCompileException, MimicException, NotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		// GIVEN
		src.addField(new CtField(CtClass.intType, "foo", src));
		
		//WHEN
		mimicCreator.mimicFields(src, dst);

		// THEN
		CtField fooField = dst.getField("foo");
		assertNotNull(fooField);
		CtClass fooFieldType = fooField.getType();
		assertEquals(CtClass.intType, fooFieldType);
		Object dstInstance = ClassPool.getDefault().toClass(dst).newInstance();
		Field realFooField = dstInstance.getClass().getDeclaredField("foo");
		assertNotNull(realFooField);

	}
	
	@Test
	public void testMimicConstructors() throws CannotCompileException, MimicException, NotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException, NoSuchFieldException {
		// GIVEN
		src.addField(new CtField(CtClass.intType, "foo", src));
		src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }", src));
		
		// WHEN
		mimicCreator.mimicFields(src, dst);
		mimicCreator.mimicConstructors(src, dst);
		
		// THEN
		CtField fooField = dst.getField("foo");
		assertNotNull(fooField);
		CtClass fooFieldType = fooField.getType();
		assertEquals(CtClass.intType, fooFieldType);
		CtConstructor constructor = dst.getConstructor(Descriptor.ofConstructor(null));
		assertNotNull(constructor);
		//we also need to check if code has been copied
		Object dstInstance = ClassPool.getDefault().toClass(dst).newInstance();
		Field realFooField = dstInstance.getClass().getDeclaredField("foo");
		realFooField.setAccessible(true);
		assertEquals(2, realFooField.get(dstInstance));
	}
	
	@Test
	public void testMimicMethods() throws CannotCompileException, MimicException, NotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		// GIVEN
		src.addMethod(CtNewMethod.make("public boolean foo() { return true;}", src));

		// WHEN
		mimicCreator.mimicMethods(src, dst);

		// THEN
		CtMethod fooMethod = dst.getDeclaredMethod("foo");
		assertNotNull(fooMethod);
		//we also need to check if code has been copied
		Object dstInstance = ClassPool.getDefault().toClass(dst).newInstance();
		Method realFooMethod = dstInstance.getClass().getMethod("foo");
		assertEquals(true, realFooMethod.invoke(dstInstance));
	}

}
