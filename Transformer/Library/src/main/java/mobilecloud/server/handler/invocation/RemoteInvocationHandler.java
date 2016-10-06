package mobilecloud.server.handler.invocation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.NonNull;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.ClassUtils;

/**
 * A class which handles remote invocation requests
 */
public class RemoteInvocationHandler implements Handler {
    
    private Server server;
    
    public RemoteInvocationHandler(@NonNull Server server) {
        this.server = server;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof RemoteInvocationRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        RemoteInvocationRequest invocReq = (RemoteInvocationRequest) request;
        RemoteInvocationResponse resp = new RemoteInvocationResponse();
        ClassLoader loader = server.getClassLoader(invocReq.getApplicationId());
        if (loader == null) {
            return resp.setSuccess(false)
                    .setThrowable(new NoApplicationExecutableException(
                            "Cannot find executable for application " + invocReq.getApplicationId()
                                    + ", please send application executable to server and try it again."));
        }
        try {
            Class<?> clazz = ClassUtils.loadClass(loader, invocReq.getClazzName());
            String[] argTypesName = invocReq.getArgTypesName();
            Class<?>[] argTypes = new Class<?>[argTypesName.length];
            for (int i = 0; i < argTypesName.length; i++) {
                argTypes[i] = ClassUtils.loadClass(loader, argTypesName[i]);
            }
            Method method = clazz.getDeclaredMethod(invocReq.getMethodName(), argTypes);
            method.setAccessible(true);
            resp.setInvoker(invocReq.getInvoker()).setArgs(invocReq.getArgs());
            Serializable ret = (Serializable) method.invoke(invocReq.getInvoker(), (Object[]) invocReq.getArgs());
            return resp.setReturnValue(ret).setSuccess(true);
        } catch (InvocationTargetException e) {
            return resp.setSuccess(false).setThrowable(e.getTargetException());
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
