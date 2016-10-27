package transformer;

import java.io.Serializable;

import org.gradle.api.Project;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;

public class SerializableTransformer implements IClassTransformer {

    private final Project project;
    private CtClass serializable;
    
    public SerializableTransformer(Project project) {
        this.project = project;
        try {
            this.serializable = ClassPool.getDefault().get(Serializable.class.getName());
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
            return !ctClass.isInterface() && !isSerializable(ctClass);
        } catch (NotFoundException e) {
            throw new JavassistBuildException(e);
        }
    }
    
    private boolean isSerializable(CtClass c) throws NotFoundException {
        try {
            return c.subtypeOf(serializable);
        } catch (NotFoundException e) {
            c.getClassPool().appendPathList(ClassPathUtils.getClasspaths(project));
            return c.subtypeOf(serializable);
        }
    }

}
