package transformer;

import java.io.Serializable;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
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
        ctClass.addInterface(serializable);
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        try {
            return !ctClass.isInterface() && !Modifier.isAbstract(ctClass.getModifiers())
                    && !ctClass.subtypeOf(serializable);
        } catch (NotFoundException e) {
            throw new JavassistBuildException(e);
        }

    }

}
