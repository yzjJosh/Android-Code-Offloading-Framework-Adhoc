package transformer;

import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import mobilecloud.lib.Ignore;

public class IgnoredFieldsTransformer implements IClassTransformer {

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException {
        for(CtField f: ctClass.getDeclaredFields()) {
            if(f.hasAnnotation(Ignore.class)) {
                int modifier = f.getModifiers();
                if(!Modifier.isTransient(modifier)) {
                    System.out.println("Adding transient modifier to field " + f.toString());
                    f.setModifiers(modifier | Modifier.TRANSIENT);
                }
            }
        }
    }

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException {
        return true;
    }

}
