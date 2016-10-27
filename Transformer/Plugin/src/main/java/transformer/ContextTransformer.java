package transformer;

import org.gradle.api.Project;

import android.content.Context;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import mobilecloud.engine.Engine;

public class ContextTransformer implements IClassTransformer  {
    
    private final Project project;
    
    public ContextTransformer(Project project) {
        this.project = project;
    }

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException {
        try {
            for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
                String code = generateInitEngineCode(constructor);
                System.out.println("Generating code for " + constructor.getLongName() + ":\n" + code);
                constructor.insertAfter(code);
            }
        } catch (Exception e) {
            throw new JavassistBuildException(e);
        }
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        try{
            return isContext(ctClass);
        } catch (NotFoundException e) {
            throw new JavassistBuildException(e);
        }
    }
    
    private boolean isContext(CtClass c) throws NotFoundException {
        if(c == null) {
            return false;
        } if(c.getName().equals(Context.class.getName())) {
            return true;
        } else {
            CtClass superClass = null;
            try {
                superClass = c.getSuperclass();
            } catch(NotFoundException e) {
                //If cannot find super class, we use backup classpath and try another time
                c.getClassPool().appendPathList(ClassPathUtils.getClasspaths(project));
                superClass = c.getSuperclass();
            }
            return isContext(superClass);
        }
    }
    
    private String generateInitEngineCode(CtConstructor ct) {
        StringBuilder code = new StringBuilder();
        //mobilecloud.engine.Engine.localInit(this);
        code.append(Engine.class.getName() + ".localInit(this);\n");
        return code.toString();
    }

}
