package transformer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import mobilecloud.engine.Engine;
import mobilecloud.lib.Remote;

public class RemoteMethodTransformer implements IClassTransformer {
    
    private static final Map<String, String> primitiveToBoxingType = new HashMap<>();
    static {
        primitiveToBoxingType.put(void.class.getName(), Void.class.getName());
        primitiveToBoxingType.put(boolean.class.getName(), Boolean.class.getName());
        primitiveToBoxingType.put(byte.class.getName(), Byte.class.getName());
        primitiveToBoxingType.put(char.class.getName(), Character.class.getName());
        primitiveToBoxingType.put(short.class.getName(), Short.class.getName());
        primitiveToBoxingType.put(int.class.getName(), Integer.class.getName());
        primitiveToBoxingType.put(long.class.getName(), Long.class.getName());
        primitiveToBoxingType.put(float.class.getName(), Float.class.getName());
        primitiveToBoxingType.put(double.class.getName(), Double.class.getName());
    }

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException {
        Set<CtMethod> visited = new HashSet<>();
        try {
            // Firstly transform all methods
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                // If ctClass is an interface, getDeclaredMethods() may contain
                // duplicate methods. Thus need to remove duplicates
                if (!visited.add(method)) {
                    continue;
                }
                // Find a method that contains @Remote annotation
                if (method.hasAnnotation(Remote.class)) {
                    // Do transformation
                    transformMethod(method);
                }
            }
        } catch (Exception e) {
            throw new JavassistBuildException(e);
        }
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        return true;
    }
    
    private void transformMethod(CtMethod method) throws CannotCompileException, NotFoundException {
        String code = generateRemoteExecutionCode(method);
        System.out.println("Generating code for method " + method.getLongName() + ": \n" + code);
        method.insertBefore(code);
    }
    
    private String generateRemoteExecutionCode(CtMethod method) throws NotFoundException {
        StringBuilder code = new StringBuilder();
        
        // try {
        code.append("try{\n");

        //     java.lang.reflect.Method __method__ = HelloWorld.class.getDeclaredMethod("helloWord", java.lang.String.class);
        String reflectionMethodVarName = "__method__";
        code.append("    " + Method.class.getName() + " " + reflectionMethodVarName + " = "
                + generateGetDeclaredMethodCall(method) + ";\n");

        //     if(mobilecode.engine.Engine.getInstance().shouldMigrate(__method__, this, name) {
        code.append("    if(" + generateShouldMigrateCall(reflectionMethodVarName, method) + ") {\n");

        //         Object __result__ = mobilecode.engine.Engine.getInstance().invokeRemotely(__method__, this, name);
        String resultVarName = "__result__";
        code.append("        Object " + resultVarName + " = "
                + generateInvokeRemotelyCall(reflectionMethodVarName, method) + ";\n");

        //         return (String) __result__;
        code.append("        " + generateReturnStatement(resultVarName, method) + ";\n");
        
        //     }
        code.append("    }\n");
        
        // } catch (java.lang.Exception e) {
        code.append("} catch (" + Exception.class.getName() + " e) {\n");
        
        //     e.printStackTrace();
        code.append("    e.printStackTrace();\n");
        
        // }
        code.append("}\n");
        
        return code.toString();
    }
    
    
    private String generateGetDeclaredMethodCall(CtMethod method) throws NotFoundException {
        StringBuilder code = new StringBuilder();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        code.append(className + ".class.getDeclaredMethod(\"" + methodName + "\", ");
        CtClass[] paramTypes = method.getParameterTypes();
        if(paramTypes.length == 0) {
            code.append("new Class[0]");
        } else {
            code.append("new Class[]{");
            for(int i=0; i < paramTypes.length; i++) {
                if(i > 0) {
                    code.append(", ");
                }
                code.append(paramTypes[i].getName() + ".class");
            }
            code.append("}");
        }
        code.append(')');
        return code.toString();
    }
    
    private String generateShouldMigrateCall(String reflectionMethodVarName, CtMethod method) throws NotFoundException {
        StringBuilder code = new StringBuilder();
        code.append(Engine.class.getName() + ".getInstance()." + "shouldMigrate(" + reflectionMethodVarName);
        if (Modifier.isStatic(method.getModifiers())) {
            code.append(", null");
        } else {
            code.append(", this");
        }
        CtClass[] paramTypes = method.getParameterTypes();
        if(paramTypes.length == 0) {
            code.append(", new Object[0]");
        } else {
            code.append(", new Object[]{");
            for (int i = 1; i <= paramTypes.length; i++) {
                if(i > 1) {
                    code.append(", ");
                }
                code.append("$" + i);
            }
            code.append("}");
        }
        code.append(')');
        return code.toString();
    }
    
    private String generateInvokeRemotelyCall(String reflectionMethodVarName, CtMethod method) throws NotFoundException {
        StringBuilder code = new StringBuilder();
        code.append(Engine.class.getName() + ".getInstance()." + "invokeRemotely(" + reflectionMethodVarName);
        if (Modifier.isStatic(method.getModifiers())) {
            code.append(", null");
        } else {
            code.append(", this");
        }
        CtClass[] paramTypes = method.getParameterTypes();
        if(paramTypes.length == 0) {
            code.append(", new Object[0]");
        } else {
            code.append(", new Object[]{");
            for (int i = 1; i <= paramTypes.length; i++) {
                if(i > 1) {
                    code.append(", ");
                }
                code.append("$" + i);
            }
            code.append("}");
        }
        code.append(')');
        return code.toString();
    }

    private String generateReturnStatement(String resultVarName, CtMethod method) throws NotFoundException {
        StringBuilder code = new StringBuilder("return ");
        CtClass returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if(!returnType.getName().equals("void")) {
                code.append("((" + primitiveToBoxingType.get(returnType.getName()) + ")" + resultVarName + ")."
                        + returnType.getName() + "Value()");
            }
        } else {
            code.append("(" + returnType.getName() + ")" + resultVarName);
        }
        return code.toString();
    }
    
}
