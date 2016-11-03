package mobilecloud.server.handler.invocation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.RemoteInvocationResponse;
import mobilecloud.api.response.Response;
import mobilecloud.lib.Ignore;
import mobilecloud.objs.ObjDiff;
import mobilecloud.objs.ObjectVisitor;
import mobilecloud.objs.OnObjectVisitedListener;
import mobilecloud.objs.Token;
import mobilecloud.objs.Token.SnapShot;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.ClassUtils;

/**
 * A class which handles remote invocation requests
 */
public class RemoteInvocationHandler implements Handler {
    
    private ExecutableLoader exeLoader;
    
    public RemoteInvocationHandler(ExecutableLoader exeLoader) {
        this.exeLoader = exeLoader;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof RemoteInvocationRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        RemoteInvocationRequest invocReq = (RemoteInvocationRequest) request;
        RemoteInvocationResponse resp = new RemoteInvocationResponse();
        
        ClassLoader loader = null;
        try {
            loader = exeLoader.loadExecutable(invocReq.getApplicationId());
        } catch (NoApplicationExecutableException e) {
            return resp.setSuccess(false)
                    .setThrowable(new NoApplicationExecutableException(
                            "Cannot find executable for application " + invocReq.getApplicationId()
                                    + ", please send application executable to server and try it again."));
        }
        
        try {
            
            // Prepare data
            Class<?> declareClazz = ClassUtils.loadClass(loader, invocReq.getClazzName());
            String[] argTypesName = invocReq.getArgTypesName();
            Class<?>[] argTypes = new Class<?>[argTypesName.length];
            for (int i = 0; i < argTypesName.length; i++) {
                argTypes[i] = ClassUtils.loadClass(loader, argTypesName[i]);
            }
            Method method = declareClazz.getDeclaredMethod(invocReq.getMethodName(), argTypes);
            
            // Take first snap shot
            Token token = invocReq.getToken();
            SnapShot snapShotOnReceiving = token.takeSnapShot();
            
            // Invoke the method
            method.setAccessible(true);
            Object ret = method.invoke(invocReq.getInvoker(), invocReq.getArgs());
            
            // Add return value to token
            if(ret != null && !ClassUtils.isBasicType(ret.getClass()) && !token.contains(ret)) {
                token = new Token.Builder(token).addObject(ret).build();
            }
            
            //Expand token
            token.expand();
            
            // Calculate diffs
            TIntObjectMap<ObjDiff> diffs = token.takeSnapShot().diff(snapShotOnReceiving);
            
            // Add only new objects to return token and trim them
            Token.Builder builder = new Token.Builder();
            ObjectVisitor visitor = new ObjectVisitor(new ObjectTrimer());
            TIntIterator it = diffs.keySet().iterator();
            while(it.hasNext()) {
                int id = it.next();
                if(snapShotOnReceiving.contains(id)) {
                    // If an old object is dirty, we do not need to send it back
                    // because we already have a client-side copy of it. We can
                    // recover it using the diff.
                    continue;
                }
                
                // Add a new object to back token
                Object obj = token.getObject(id);
                builder.addObject(id, obj);
                
                if (ClassUtils.isPrimitiveArray(obj.getClass())) {
                    // If this obejct is a primitive array, it is safe to remove
                    // the diff of this object, because its content will never
                    // point to client-side objects.
                    it.remove();
                    continue;
                }
                visitor.withObject(obj);
            }
            visitor.visitFields();
            Token backToken = builder.build();
            
            return resp.setToken(backToken).setDiffs(diffs).setReturnVal(ret).setSuccess(true);
        } catch (InvocationTargetException e) {
            return resp.setSuccess(false).setThrowable(e.getTargetException());
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }
    
    private class ObjectTrimer implements OnObjectVisitedListener {
        @Override
        public boolean onObjectVisited(Object obj, Object array, int index) {
            if(ClassUtils.isPrimitiveArray(obj.getClass())) {
                return false;
            } else {
                Array.set(array, index, null);
                return true;
            }
        }

        @Override
        public boolean onObjectVisited(Object obj, Object from, Field field) {
            int modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier) || field.isAnnotationPresent(Ignore.class)) {
                // ignore static, ignored, and final fields
                return false;
            }
            if (field.getType().isPrimitive()) {
                // ignore primitive fields
                return false;
            }
            try {
                // Set pointers to null
                field.set(from, null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

}
