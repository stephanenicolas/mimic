package com.github.stephanenicolas.mimic;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


/**
 * Enables mimicing a class. Mimicing is, indeed, kind of way to bypass java
 * single inheritance paradigm. It allows to copy all declared fields,
 * constructors and methods from a given class into another class. For instance
 * if we have
 * 
 * <pre>
 * public class Src {
 *     private int a;
 * 
 *     public Src() {...}
 *     protected b() {...}
 *     protected c() {...}
 * }
 * </pre>
 * and
 * <pre>
 * public class Dst {
 *     protected c() {...}
 * }
 * </pre>
 * it will result in
 * <pre>
 * public class Dst {
 *   public Dst() {...//calling the code of Src() in the end}
 *   private int a;
 *   protected b() {...}
 *   protected c() {...//calling the code of Src.c() in the end}
 * }
 * </pre>
 * @author SNI
 */
public class MimicCreator {

    /** A key used to distinguish possibly conflicting copies of a src methods. */
    private String key;

    public MimicCreator() {
    }

    /**
     * Gives a {@link #key} to the instance. When mimicing constructors or
     * methods, if dst and src share a method with the same name, we have to
     * create a copy of the src method and invoke it from dst methods. The
     * parameter key will be added to the copy name to avoid conflicts.
     * @param key
     *            used to distinguish possibly conflicting copies of a src
     *            methods.
     */
    public MimicCreator(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * Copies all fields, constructors and methods declared in class src into
     * dst. All interfaces implemented by src will also be implemented by dst.
     * @param src
     *            the src class.
     * @param dst
     *            the dst class.
     * @throws NotFoundException
     *             should not be thrown.
     * @throws CannotCompileException
     *             should not be thrown except if class src doesn't compile...
     * @throws MimicException
     *             if mimicing is not possible. For instance if class src and
     *             dst share a common field.
     */
    public void mimicClass(CtClass src, CtClass dst) throws NotFoundException,
            CannotCompileException, MimicException {
        mimicInterfaces(src, dst);
        mimicFields(src, dst);
        mimicConstructors(src, dst);
        mimicMethods(src, dst);
    }

    public void mimicMethods(CtClass src, CtClass dst) throws CannotCompileException {
        for (final CtMethod method : src.getDeclaredMethods()) {
            System.out.println("Mimic method " + method.getName());
            boolean destHasSameMethod = false;
            for (CtMethod methodInDest : dst.getDeclaredMethods()) {
                String signature = method.getSignature() + method.getName();
                String signatureInDest = methodInDest.getSignature()
                        + methodInDest.getName();
                if (signatureInDest.equals(signature)) {
                    destHasSameMethod = true;
                    System.out.println("Forwarding " + method.getName());
                    String key = this.key == null ? "" : (this.key + "_");
                    final String copiedMethodName = "_copy_" + key
                            + method.getName();
                    dst.addMethod(CtNewMethod.copy(method, copiedMethodName,
                            dst, null));
                    methodInDest.instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            try {
                                // call to super
                                if (m.getMethodName().equals(method.getName())) {
                                    StringBuffer buffer = new StringBuffer();
                                    for (int j = 0; j < method.getParameterTypes().length; j++) {
                                        buffer.append(" $");
                                        buffer.append(j + 1);
                                        buffer.append(",");
                                    }
                                    String params = buffer.toString();
                                    if (params.length() > 0) {
                                        params = params.substring(0,
                                                params.length() - 1);
                                    }
                                    String string = copiedMethodName + "("
                                            + params + ");\n";
                                    System.out.println("swinged " + string);
                                    m.replace(string);
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                            super.edit(m);
                        }
                    });
                }
            }
            if (!destHasSameMethod) {
                System.out.println("Copying " + method.getName());
                dst.addMethod(CtNewMethod.copy(method, dst, null));
            }
        }
    }

    public void mimicConstructors(CtClass src, CtClass dst) throws CannotCompileException, NotFoundException {
        for (final CtConstructor constructor : src.getDeclaredConstructors()) {
            System.out.println("Mimic constructor " + constructor.getName());
            boolean destHasSameConstructor = false;
            for (CtConstructor constructorInDest : dst
                    .getDeclaredConstructors()) {
                String signature = constructor.getSignature();
                String signatureInDest = constructorInDest.getSignature();
                if (signatureInDest.equals(signature)) {
                    destHasSameConstructor = true;
                    System.out.println("Forwarding " + constructor.getName());
                    String key = this.key == null ? "" : (this.key + "_");
                    final String copiedConstructorName = "_copy_" + key
                            + constructor.getName();
                    dst.addMethod(constructor.toMethod(copiedConstructorName,
                            dst));
                    StringBuffer buffer = new StringBuffer();
                    for (int j = 0; j < constructor.getParameterTypes().length; j++) {
                        buffer.append(" $");
                        buffer.append(j + 1);
                        buffer.append(",");
                    }
                    String params = buffer.toString();
                    if (params.length() > 0) {
                        params = params.substring(0, params.length() - 1);
                    }
                    String string = copiedConstructorName + "(" + params
                            + ");\n";
                    System.out.println("swinged " + string);
                    constructorInDest.insertAfter(string);
                }
            }
            if (!destHasSameConstructor) {
                System.out.println("Copying " + constructor.getName());
                dst.addConstructor(CtNewConstructor
                        .copy(constructor, dst, null));
            }
        }
    }

    public void mimicFields(CtClass src, CtClass dst) throws MimicException,
            CannotCompileException {
        for (CtField field : src.getDeclaredFields()) {
            if (hasField(dst, field)) {
                throw new MimicException(String.format(
                        "Class %s already has a field named %s %n",
                        dst.getName(), field.getName()));
            }
            dst.addField(new CtField(field, dst));
        }
    }

    public void mimicInterfaces(CtClass src, CtClass dst) throws NotFoundException {
        for (CtClass interfazz : src.getInterfaces()) {
            if (!hasInterface(dst, interfazz)) {
                dst.addInterface(interfazz);
            }
        }
    }

    public boolean hasInterface(CtClass dst, CtClass interfazz) throws NotFoundException {
        for (CtClass interfazzInClass : dst.getInterfaces()) {
            if (interfazzInClass.getName().equals(interfazz.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasField(CtClass clazz, CtField field) {
        boolean hasField = false;
        try {
            clazz.getField(field.getName());
            hasField = true;
        } catch (Exception e) {
            // nothing
            hasField = false;
        }
        return hasField;
    }

}
