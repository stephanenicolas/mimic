package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.Descriptor;

import org.junit.Before;
import org.junit.Test;

public class MimicCreatorTest {

    private MimicCreator mimicCreator;
    private CtClass src;
    private CtClass dst;

    @Before
    public void setUp() {
        mimicCreator = new MimicCreator();
        src = ClassPool.getDefault().makeClass("Src" + TestCounter.testCounter);
        dst = ClassPool.getDefault().makeClass("Dst" + TestCounter.testCounter);
        TestCounter.testCounter++;
    }

    @Test
    public void testMimicInterfaces() throws Exception {
        // GIVEN
        CtClass interfazz = ClassPool.getDefault().makeInterface(
                "Able" + TestCounter.testCounter);
        src.addInterface(interfazz);
        // to load the interface class
        Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

        // WHEN
        mimicCreator.mimicInterfaces(src, dst);

        // THEN
        assertHasInterface(interfaceClass, ClassPool.getDefault().toClass(dst));
    }

    @Test
    public void testMimicFields() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));

        // WHEN
        mimicCreator.mimicFields(src, dst);

        // THEN
        assertHasFooField(null);
    }

    @Test
    public void testMimicConstructors() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }",
                src));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertHasFooFieldAndConstructor(ClassPool.getDefault().toClass(dst));
    }
    
    @Test
    public void testMimicConstructors_with_same_constructors() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }",
                src));
        dst.addConstructor(CtNewConstructor.make("public Dst() {}",
                dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertHasFooFieldAndConstructor(ClassPool.getDefault().toClass(dst));
    }

    @Test
    public void testMimicMethods() throws Exception {
        // GIVEN
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}",
                src));

        // WHEN
        mimicCreator.mimicMethods(src, dst);

        // THEN
        assertHasFooMethod(ClassPool.getDefault().toClass(dst));
    }

    @Test
    public void testMimicMethods_with_same_methods() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true; }", src));
        dst.addMethod(CtNewMethod
                .make("public boolean foo() { return false;}", dst));
        
        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_RETURN);

        // THEN
        assertHasFooMethod(dst.toClass());
    }

    @Test
    public void testMimicClass() throws Exception {
        // GIVEN
        CtClass interfazz = ClassPool.getDefault().makeInterface(
                "Able" + TestCounter.testCounter);
        src.addInterface(interfazz);
        // to load the interface class
        Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }",
                src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}",
                src));

        // WHEN
        mimicCreator.mimicClass(src, dst);

        // THEN
        Class<?> dstClass = ClassPool.getDefault().toClass(dst);

        assertHasInterface(interfaceClass, dstClass);
        assertHasFooFieldAndConstructor(dstClass);
        assertHasFooMethod(dstClass);
    }

    private void assertHasInterface(Class<?> interfaceClass, Class<?> dstClass) throws Exception {
        CtClass fooInterface = dst.getInterfaces()[0];
        assertNotNull(fooInterface);
        Class<?> realInterface = dstClass.getInterfaces()[0];
        assertEquals(realInterface, interfaceClass);
    }

    private void assertHasFooMethod(Class<?> dstClass) throws Exception {
        CtMethod fooMethod = dst.getDeclaredMethod("foo");
        assertNotNull(fooMethod);
        // we also need to check if code has been copied
        Object dstInstance = dstClass.newInstance();
        Method realFooMethod = dstInstance.getClass().getMethod("foo");
        assertEquals(true, realFooMethod.invoke(dstInstance));
    }

    private void assertHasFooFieldAndConstructor(Class<?> dstClass) throws Exception {
        CtField fooField = dst.getField("foo");
        assertNotNull(fooField);
        CtClass fooFieldType = fooField.getType();
        assertEquals(CtClass.intType, fooFieldType);
        CtConstructor constructor = dst.getConstructor(Descriptor
                .ofConstructor(null));
        assertNotNull(constructor);
        // we also need to check if code has been copied
        Object dstInstance = dstClass.newInstance();
        Field realFooField = dstInstance.getClass().getDeclaredField("foo");
        realFooField.setAccessible(true);
        assertEquals(2, realFooField.get(dstInstance));
    }

    private void assertHasFooField(Integer value) throws Exception {
        CtField fooField = dst.getField("foo");
        assertNotNull(fooField);
        CtClass fooFieldType = fooField.getType();
        assertEquals(CtClass.intType, fooFieldType);
        Object dstInstance = ClassPool.getDefault().toClass(dst).newInstance();
        Field realFooField = dstInstance.getClass().getDeclaredField("foo");
        assertNotNull(realFooField);
        if (value != null) {
            Method realFooMethod = dstInstance.getClass().getMethod("foo");
            realFooMethod.invoke(dstInstance);
            realFooField.setAccessible(true);
            assertEquals(value, realFooField.get(dstInstance));
        }
    }

}
