package transformer;

import android.content.Context;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import mobilecloud.engine.Engine;

public class ContextTransformer implements IClassTransformer  {

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
        try {
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
            return isContext(c.getSuperclass());
        }
    }
    
    private String generateInitEngineCode(CtConstructor ct) {
        StringBuilder code = new StringBuilder();
        //mobilecloud.engine.Engine.localInit(this);
        code.append(Engine.class.getName() + ".localInit(this);\n");
        return code.toString();
    }

}
