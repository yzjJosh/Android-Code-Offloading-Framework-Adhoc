package transformer;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import mobilecloud.annotation.Remote;

public class ClassTransformer implements IClassTransformer {

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException {
        try {        
            for (CtMethod method : ctClass.getMethods()) {
                if(method.hasAnnotation(Remote.class)) {
                    method.insertAfter("System.out.println(\"I am inserted code!\");");
                }
            }
        } catch (Exception e) {
            throw new JavassistBuildException(e);
        }
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        return ctClass.getName().contains("example.helloword.MainActivity");
    }

}
