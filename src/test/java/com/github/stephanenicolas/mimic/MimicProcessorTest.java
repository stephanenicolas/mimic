package com.github.stephanenicolas.mimic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.mimic.annotations.Mimic;
import com.github.stephanenicolas.mimic.annotations.MimicMethod;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;

public class MimicProcessorTest {

    private MimicProcessor mimicProcessor;
    private CtClass src;
    private CtClass dst;

    private void addMimicAnnotation(CtClass dst, String sourceClassName,
            boolean isMimicingInterfaces, boolean isMimicingFields,
            boolean isMimicingConstructors, boolean isMimicingMethods) {
        ClassFile cf = dst.getClassFile();
        ConstPool cp = cf.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(cp,
                AnnotationsAttribute.visibleTag);

        Annotation a = new Annotation(Mimic.class.getName(), cp);
        a.addMemberValue("sourceClass", new ClassMemberValue(sourceClassName,
                cp));
        a.addMemberValue("isMimicingInterfaces", new BooleanMemberValue(
                isMimicingInterfaces, cp));
        a.addMemberValue("isMimicingFields", new BooleanMemberValue(
                isMimicingFields, cp));
        a.addMemberValue("isMimicingConstructors", new BooleanMemberValue(
                isMimicingConstructors, cp));
        a.addMemberValue("isMimicingMethods", new BooleanMemberValue(
                isMimicingMethods, cp));
        attr.setAnnotation(a);
        cf.addAttribute(attr);
        cf.setVersionToJava5();
    }

    @Before
    public void setUp() {
        mimicProcessor = new MimicProcessor();
        src = ClassPool.getDefault().makeClass("Src" + TestCounter.testCounter);
        dst = ClassPool.getDefault().makeClass("Dst" + TestCounter.testCounter);
        TestCounter.testCounter++;
    }

    @Test
    public void testShouldTransform_does_filter_correctly_a_mimic_annotation()
            throws Exception {
        addMimicAnnotation(dst, src.getName(), true, true, true, true);

        // WHEN
        boolean filter = mimicProcessor.shouldTransform(dst);

        // THEN
        assertTrue(filter);
    }

    @Test
    public void testShouldTransform_does_filter_correctly_a_class_with_no_mimic_annotation()
            throws Exception {
        // GIVEN

        // WHEN
        boolean filter = mimicProcessor.shouldTransform(dst);

        // THEN
        assertFalse(filter);
    }

    @Test
    public void testShouldTransform_does_filter_correctly_a_non_static_inner_class() throws Exception {
        // GIVEN
        dst = new CtClass("Dst" + TestCounter.testCounter) {

            @Override
            public CtClass[] getDeclaredClasses() throws NotFoundException {
                CtClass inner = new CtClass("Inner" + TestCounter.testCounter) {
                    @Override
                    public CtClass getDeclaringClass() throws NotFoundException {
                        return dst;
                    }
                    @Override
                    public int getModifiers() {
                        return Modifier.PUBLIC;
                    }
                };

                return new CtClass[] {inner};
            }
        };


        // WHEN
        boolean filter = mimicProcessor.shouldTransform(dst.getDeclaredClasses()[0]);

        // THEN
        assertFalse(filter);
    }

    @Test
    public void testTransform_with_mimic_constructors_only() throws Exception {
        // GIVEN
        addMimicAnnotation(dst, TestSourceClass.class.getName(), false, false,
                true, false);
        final MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
        Guice.createInjector(new MimicCreatorTestModule(mimicMock))
        .injectMembers(mimicProcessor);
        mimicMock.mimicConstructors(
                EasyMock.eq(ClassPool.getDefault().get(
                        TestSourceClass.class.getName())), EasyMock.eq(dst));
        EasyMock.replay(mimicMock);

        // WHEN
        mimicProcessor.applyTransformations(dst);

        // THEN
        EasyMock.verify(mimicMock);
    }

    @Test
    public void testTransform_with_mimic_defaults() throws Exception {
        // GIVEN
        addMimicAnnotation(dst, TestSourceClass.class.getName(), true, true,
                true, true);
        final MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
        Guice.createInjector(new MimicCreatorTestModule(mimicMock))
        .injectMembers(mimicProcessor);
        mimicMock.mimicClass(
                EasyMock.eq(ClassPool.getDefault().get(
                        TestSourceClass.class.getName())), EasyMock.eq(dst),
                        (MimicMode) EasyMock.anyObject(),
                        (MimicMethod[]) EasyMock.anyObject());
        EasyMock.replay(mimicMock);

        // WHEN
        mimicProcessor.applyTransformations(dst);

        // THEN
        EasyMock.verify(mimicMock);
    }

    @Test
    public void testTransform_with_mimic_fields_only() throws Exception {
        // GIVEN
        addMimicAnnotation(dst, TestSourceClass.class.getName(), false, true,
                false, false);
        final MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
        Guice.createInjector(new MimicCreatorTestModule(mimicMock))
        .injectMembers(mimicProcessor);
        mimicMock.mimicFields(
                EasyMock.eq(ClassPool.getDefault().get(
                        TestSourceClass.class.getName())), EasyMock.eq(dst));
        EasyMock.replay(mimicMock);

        // WHEN
        mimicProcessor.applyTransformations(dst);

        // THEN
        EasyMock.verify(mimicMock);
    }

    @Test
    public void testTransform_with_mimic_interfaces_only() throws Exception {
        // GIVEN
        addMimicAnnotation(dst, TestSourceClass.class.getName(), true, false,
                false, false);
        final MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
        Guice.createInjector(new MimicCreatorTestModule(mimicMock))
        .injectMembers(mimicProcessor);
        mimicMock.mimicInterfaces(
                EasyMock.eq(ClassPool.getDefault().get(
                        TestSourceClass.class.getName())), EasyMock.eq(dst));
        EasyMock.replay(mimicMock);

        // WHEN
        mimicProcessor.applyTransformations(dst);

        // THEN
        EasyMock.verify(mimicMock);
    }

    @Test
    public void testTransform_with_mimic_methods_only() throws Exception {
        // GIVEN
        addMimicAnnotation(dst, TestSourceClass.class.getName(), false, false,
                false, true);
        final MimicCreator mimicMock = EasyMock.createMock(MimicCreator.class);
        Guice.createInjector(new MimicCreatorTestModule(mimicMock))
        .injectMembers(mimicProcessor);
        mimicMock.mimicMethods(
                EasyMock.eq(ClassPool.getDefault().get(
                        TestSourceClass.class.getName())), EasyMock.eq(dst),
                        (MimicMode) EasyMock.anyObject(),
                        (MimicMethod[]) EasyMock.anyObject());
        EasyMock.replay(mimicMock);

        // WHEN
        mimicProcessor.applyTransformations(dst);

        // THEN
        EasyMock.verify(mimicMock);
    }

    private final class MimicCreatorTestModule extends AbstractModule {
        private final MimicCreator mimicMock;

        private MimicCreatorTestModule(MimicCreator mimicMock) {
            this.mimicMock = mimicMock;
        }

        @Override
        protected void configure() {
            bind(MimicCreator.class).toInstance(mimicMock);
        }
    }

}
