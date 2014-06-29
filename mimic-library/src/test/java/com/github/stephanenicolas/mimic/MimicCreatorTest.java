package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
import javassist.Modifier;
import javassist.bytecode.Descriptor;

import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.mimic.annotations.MimicMethod;
import com.github.stephanenicolas.mimic.annotations.MimicMode;

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
    public void testMimicClass() throws Exception {
        // GIVEN
        CtClass interfazz = ClassPool.getDefault().makeInterface("Able" + TestCounter.testCounter);
        src.addInterface(interfazz);
        // to load the interface class
        Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}", src));

        // WHEN
        mimicCreator.mimicClass(src, dst, MimicMode.AFTER_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();

        assertHasInterface(interfaceClass, dstClass);
        assertHasFooFieldAndConstructor(dstClass);
        assertHasFooMethod(dst, dstClass);
    }

    @Test
    public void testMimicConstructors() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }", src));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertHasFooFieldAndConstructor(dst.toClass());
    }

    @Test
    public void testMimicConstructors_with_same_constructors() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addConstructor(CtNewConstructor.make("public Src() { foo = 2; }", src));
        dst.addConstructor(CtNewConstructor.make("public Dst() {}", dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertHasFooFieldAndConstructor(dst.toClass());
    }

    @Test
    public void testMimicConstructors_with_keys() throws Exception {
        // GIVEN
        mimicCreator = new MimicCreator("bar");
        src.addConstructor(CtNewConstructor.make("public Src() {}", src));
        dst.addConstructor(CtNewConstructor.make("public Dst() {}", dst));

        // WHEN
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertEquals("bar", mimicCreator.getKey());
        assertHasMethod(dst.toClass(), "_copy_bar_" + src.getName(), null);
    }

    @Test
    public void testMimicConstructors_with_same_constructor_with_params() throws Exception {
        // GIVEN
        mimicCreator = new MimicCreator("bar");
        src.addConstructor(CtNewConstructor.make("public Src(int a) {}", src));
        dst.addConstructor(CtNewConstructor.make("public Dst(int a) {}", dst));

        // WHEN
        mimicCreator.mimicConstructors(src, dst);

        // THEN
        assertHasConstructor(dst, dst.toClass(), new CtClass[] { CtClass.intType });
    }

    @Test
    public void testMimicFields() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));

        // WHEN
        mimicCreator.mimicFields(src, dst);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        assertHasFooField(dstClass.newInstance(), 0);
    }

    @Test(expected = MimicException.class)
    public void testMimicFields_with_same_field() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        dst.addField(new CtField(CtClass.booleanType, "foo", dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);

        // THEN
        // exception
    }

    @Test
    public void testMimicInterfaces() throws Exception {
        // GIVEN
        CtClass interfazz = ClassPool.getDefault().makeInterface("Able" + TestCounter.testCounter);
        src.addInterface(interfazz);
        // to load the interface class
        Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

        // WHEN
        mimicCreator.mimicInterfaces(src, dst);

        // THEN
        assertHasInterface(interfaceClass, dst.toClass());
    }

    @Test
    public void testMimicInterfaces_with_same_interfaces() throws Exception {
        // GIVEN
        CtClass interfazz = ClassPool.getDefault().makeInterface("Able" + TestCounter.testCounter);
        src.addInterface(interfazz);
        dst.addInterface(interfazz);
        // to load the interface class
        Class<?> interfaceClass = ClassPool.getDefault().toClass(interfazz);

        // WHEN
        mimicCreator.mimicInterfaces(src, dst);

        // THEN
        assertHasInterface(interfaceClass, dst.toClass());
    }

    @Test
    public void testMimicMethods() throws Exception {
        // GIVEN
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}", src));

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.AFTER_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooMethod(dst, dstClass);
    }

    @Test(expected=CannotCompileException.class)
    public void testMimicMethods_with_same_methods_but_non_existing_param() throws Exception {
        // GIVEN
        CtClass paramClass = ClassPool.getDefault().makeClass("Param" + TestCounter.testCounter);

        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo(" + paramClass.getName() + " a) { System.out.println(); return true; }", src));
        dst.addMethod(CtNewMethod.make("public boolean foo(" + paramClass.getName() + " a) { System.out.println(); return false;}", dst));

        paramClass.detach();

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE, new MimicMethod[0]);

        // THEN
        //should fail
    }

    @Test
    public void testMimicMethods_with_same_methods() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true; }", src));
        dst.addMethod(CtNewMethod.make("public boolean foo() { return false;}", dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_RETURN, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooMethod(dst, dstClass);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_cutom_mimic_method() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true; }", src));
        dst.addMethod(CtNewMethod.make("public boolean foo() { return false;}", dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        MimicMethod mimicMethod = createMimicMethod(MimicMode.BEFORE_RETURN, "foo", null);
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_SUPER, new MimicMethod[] { mimicMethod });

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooMethod(dst, dstClass);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_replace_override() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return true; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        dstAncestor.addMethod(CtNewMethod.make("protected boolean foo() { return false;}", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public boolean foo() { return super.foo();}", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.REPLACE_SUPER, new MimicMethod[0]);

        // THEN
        assertHasFooMethod(dst, dst.toClass());
    }

    @Test(expected=MimicException.class)
    public void testMimicMethods_with_same_methods_with_replace_override_but_not_calling_super() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { return false; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        dstAncestor.addMethod(CtNewMethod.make("protected boolean foo(int a) { return true;}", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public boolean foo() { return foo(1);}", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.REPLACE_SUPER, new MimicMethod[0]);

        // THEN
    }

    @Test
    public void testMimicMethods_with_same_methods_with_before_super() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { foo = 2; return false ; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        CtField field = new CtField(CtClass.intType, "foo", dstAncestor);
        field.setModifiers(Modifier.PUBLIC);
        dstAncestor.addField(field);
        dstAncestor.addMethod(CtNewMethod.make("public boolean foo() { foo *= 2; return true; }", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public boolean foo() { foo = 1; return super.foo();}", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 4);
        assertHasFooMethod(dst, dstClass);
    }

    @Test(expected=MimicException.class)
    public void testMimicMethods_with_same_methods_with_before_super_but_not_calling_super() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public boolean foo() { foo = 2; return false ; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        CtField field = new CtField(CtClass.intType, "foo", dstAncestor);
        field.setModifiers(Modifier.PUBLIC);
        dstAncestor.addField(field);
        dstAncestor.addMethod(CtNewMethod.make("public boolean foo(int a) { foo *= 2; return true; }", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public boolean foo() { foo = 1; return foo(1);}", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 2);
        assertHasFooMethod(dst, dstClass);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_after_super() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 3; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        CtField field = new CtField(CtClass.intType, "foo", dstAncestor);
        field.setModifiers(Modifier.PUBLIC);
        dstAncestor.addField(field);
        dstAncestor.addMethod(CtNewMethod.make("protected void foo() { foo *=2; }", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public void foo() { foo = 2; super.foo(); }", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.AFTER_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 3);
    }

    @Test(expected=MimicException.class)
    public void testMimicMethods_with_same_methods_with_after_super_but_not_calling_super() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 3; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        CtField field = new CtField(CtClass.intType, "foo", dstAncestor);
        field.setModifiers(Modifier.PUBLIC);
        dstAncestor.addField(field);
        dstAncestor.addMethod(CtNewMethod.make("protected void foo(int a) { foo *=2; }", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);
        dst.addMethod(CtNewMethod.make("public void foo() { foo = 2; foo(1); }", dst));
        dstAncestor.toClass();

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.AFTER_SUPER, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 4);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_after() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 4; }", src));

        dst.addField(new CtField(CtClass.intType, "foo", dst));
        dst.addMethod(CtNewMethod.make("public void bar() { foo = 3; }", dst));
        dst.addMethod(CtNewMethod.make("public void foo() { foo = 2; bar(); }", dst));

        // WHEN
        MimicMethod mimicMethod = createMimicMethod(MimicMode.AFTER, "foo", "bar");
        mimicCreator.mimicMethods(src, dst, MimicMode.AFTER, new MimicMethod[] { mimicMethod });

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 4);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_before() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 4; }", src));

        dst.addField(new CtField(CtClass.intType, "foo", dst));
        dst.addMethod(CtNewMethod.make("public void bar() { foo *= 2 ; }", dst));
        dst.addMethod(CtNewMethod.make("public void foo() { foo = 2; bar(); }", dst));

        // WHEN
        MimicMethod mimicMethod = createMimicMethod(MimicMode.BEFORE, "foo", "bar");
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE, new MimicMethod[] { mimicMethod });

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 8);
    }

    @Test
    public void testMimicMethods_with_same_methods_in_super_class_with_before() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 4; }", src));

        CtClass dstAncestor = ClassPool.getDefault().makeClass("DstAncestor" + TestCounter.testCounter);
        CtField field = new CtField(CtClass.intType, "foo", dstAncestor);
        field.setModifiers(Modifier.PUBLIC);
        dstAncestor.addField(field);
        dstAncestor.addMethod(CtNewMethod.make("public void bar() {}", dstAncestor));
        dstAncestor.addConstructor(CtNewConstructor.make("public " + dstAncestor.getName() + "() {}", dstAncestor));
        dst.setSuperclass(dstAncestor);

        dst.addField(new CtField(CtClass.intType, "foo", dst));
        dst.addMethod(CtNewMethod.make("public void foo() { foo = 2; bar(); }", dst));

        // WHEN
        MimicMethod mimicMethod = createMimicMethod(MimicMode.BEFORE, "foo", "bar");
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE, new MimicMethod[] { mimicMethod });
        dstAncestor.toClass();

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 4);
    }

    @Test
    public void testMimicMethods_with_same_methods_with_at_beginning() throws Exception {
        // GIVEN
        src.addField(new CtField(CtClass.intType, "foo", src));
        src.addMethod(CtNewMethod.make("public void foo() { foo = 3; }", src));

        CtField field = new CtField(CtClass.intType, "foo", dst);
        dst.addField(field);
        dst.addMethod(CtNewMethod.make("public void foo() { foo *= 2; }", dst));

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.AT_BEGINNING, new MimicMethod[0]);

        // THEN
        Class<?> dstClass = dst.toClass();
        assertHasFooField(dst);
        Object dstInstance = dstClass.newInstance();
        invokeFoo(dstInstance);
        assertHasFooField(dstInstance, 6);
    }

    @Test
    public void testMimicMethods_with_key() throws Exception {
        // GIVEN
        mimicCreator = new MimicCreator("bar");
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}", src));
        dst.addMethod(CtNewMethod.make("public boolean foo() { return true;}", dst));

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_RETURN, new MimicMethod[0]);

        // THEN
        assertHasMethod(dst.toClass(), "_copy_bar_foo", null);
    }

    @Test(expected=MimicException.class)
    public void testMimicMethods_with_empty_after() throws Exception {
        // GIVEN
        mimicCreator = new MimicCreator("bar");
        src.addMethod(CtNewMethod.make("public boolean foo() { return true;}", src));
        dst.addMethod(CtNewMethod.make("public boolean foo() { return true;}", dst));

        // WHEN
        mimicCreator.mimicMethods(src, dst, MimicMode.AFTER, new MimicMethod[0]);

        // THEN
    }

    @Test
    public void testMimicMethods_with_same_method_with_params() throws Exception {
        // GIVEN
        src.addMethod(CtNewMethod.make("public boolean foo(int a) { return true; }", src));
        dst.addMethod(CtNewMethod.make("public boolean foo(int a) { return true;}", dst));

        // WHEN
        mimicCreator.mimicFields(src, dst);
        mimicCreator.mimicMethods(src, dst, MimicMode.BEFORE_RETURN, new MimicMethod[0]);

        // THEN
        assertHasMethod(dst.toClass(), "foo", new Class<?>[] { int.class });
    }

    private void assertHasFooField(CtClass dst) throws Exception {
        CtField fooField = dst.getField("foo");
        assertNotNull(fooField);
        CtClass fooFieldType = fooField.getType();
        assertEquals(CtClass.intType, fooFieldType);
    }

    private void assertHasFooField(Object dstInstance, Integer value) throws Exception {
        Field realFooField;
        try {
            realFooField = dstInstance.getClass().getDeclaredField("foo");
        } catch (Exception e) {
            realFooField = dstInstance.getClass().getField("foo");
        }
        assertNotNull(realFooField);
        realFooField.setAccessible(true);
        assertEquals(value, realFooField.get(dstInstance));
    }

    private void invokeFoo(Object dstInstance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method realFooMethod;
        try {
            realFooMethod = dstInstance.getClass().getDeclaredMethod("foo");
        } catch (Exception e) {
            realFooMethod = dstInstance.getClass().getMethod("foo");
        }
        realFooMethod.invoke(dstInstance);
    }

    private void assertHasFooFieldAndConstructor(Class<?> dstClass) throws Exception {
        CtField fooField = dst.getField("foo");
        assertNotNull(fooField);
        CtClass fooFieldType = fooField.getType();
        assertEquals(CtClass.intType, fooFieldType);
        CtConstructor constructor = dst.getConstructor(Descriptor.ofConstructor(null));
        assertNotNull(constructor);
        // we also need to check if code has been copied
        Object dstInstance = dstClass.newInstance();
        Field realFooField = dstInstance.getClass().getDeclaredField("foo");
        realFooField.setAccessible(true);
        assertEquals(2, realFooField.get(dstInstance));
    }

    private void assertHasConstructor(CtClass dst, Class<?> dstClass, CtClass[] paramClasses) throws Exception {
        CtConstructor fooField = dst.getConstructor(Descriptor.ofConstructor(paramClasses));
        assertNotNull(fooField);
        // we also need to check if code has been copied
        Class<?>[] paramTypes = new Class<?>[paramClasses.length];
        Object[] params = new Object[paramClasses.length];
        int indexClass = 0;
        for (CtClass ctClass : paramClasses) {
            if (ctClass == CtClass.intType) {
                paramTypes[indexClass] = int.class;
                params[indexClass] = 0;
            } else {
                paramTypes[indexClass] = ctClass.toClass();
                params[indexClass] = null;
            }
            indexClass++;
        }
        Constructor<?> dstInstance = dstClass.getConstructor(paramTypes);
        assertNotNull(dstInstance.newInstance(params));
    }

    private void assertHasFooMethod(CtClass dst, Class<?> dstClass) throws Exception {
        CtMethod fooMethod = dst.getDeclaredMethod("foo");
        assertNotNull(fooMethod);
        // we also need to check if code has been copied
        Object dstInstance = dstClass.newInstance();
        Method realFooMethod = dstInstance.getClass().getMethod("foo");
        assertEquals(true, realFooMethod.invoke(dstInstance));
    }

    private void assertHasMethod(Class<?> dstClass, String methodName, Class<?>[] paramClasses) throws Exception {
        CtMethod fooMethod = dst.getDeclaredMethod(methodName);
        assertNotNull(fooMethod);
        // we also need to check if code has been copied
        Object dstInstance = dstClass.newInstance();
        Method realFooMethod = dstInstance.getClass().getMethod(methodName, paramClasses);
        assertNotNull(realFooMethod);
        Object[] params = new Object[paramClasses == null ? 0 : paramClasses.length];
        if (paramClasses != null) {
            int indexClass = 0;
            for (Class<?> ctClass : paramClasses) {
                if (ctClass == int.class) {
                    params[indexClass] = 0;
                } else {
                    params[indexClass] = null;
                }
                indexClass++;
            }
        }
        realFooMethod.invoke(dstInstance, params);
    }

    private void assertHasInterface(Class<?> interfaceClass, Class<?> dstClass) throws Exception {
        CtClass fooInterface = dst.getInterfaces()[0];
        assertNotNull(fooInterface);
        Class<?> realInterface = dstClass.getInterfaces()[0];
        assertEquals(realInterface, interfaceClass);
    }

    private MimicMethod createMimicMethod(final MimicMode mimicMode, final String methodName, final String insertionMethod) {
        return new MimicMethod() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return MimicMethod.class;
            }

            @Override
            public MimicMode mode() {
                return mimicMode;
            }

            @Override
            public String methodName() {
                return methodName;
            }

            @Override
            public String insertionMethod() {
                return insertionMethod;
            }
        };
    }
}
