package transformer;

import java.io.Serializable;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;

public class SerializableTransformer implements IClassTransformer {
    
    private CtClass serializable;
    
    public SerializableTransformer() {
        try {
            serializable = ClassPool.getDefault().get(Serializable.class.getName());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException {
        System.out.println("Implementing java.io.Serializable for class " + ctClass.getName());
        ctClass.addInterface(serializable);
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        try {
            return !ctClass.isInterface() && !ctClass.subtypeOf(serializable);
        } catch (NotFoundException e) {
            return true;
        }

    }

}
