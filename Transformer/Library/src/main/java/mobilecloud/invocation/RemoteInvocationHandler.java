package mobilecloud.invocation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mobilecloud.server.Handler;
import mobilecloud.server.Server;
import mobilecloud.utils.ClassUtils;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

/**
 * A class which handles remote invocation requests
 */
public class RemoteInvocationHandler implements Handler {

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof RemoteInvocationRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        RemoteInvocationRequest invocReq = (RemoteInvocationRequest) request;
        RemoteInvocationResponse resp = new RemoteInvocationResponse();
        ClassLoader loader = Server.getInstance().getClassLoader(invocReq.getApplicationId());
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
            Method method = clazz.getMethod(invocReq.getMethodName(), argTypes);
            resp.setInvoker(invocReq.getInvoker()).setArgs(invocReq.getArgs());
            Serializable ret = (Serializable) method.invoke(invocReq.getInvoker(), (Object[])invocReq.getArgs());
            return resp.setReturnValue(ret).setSuccess(true);
        } catch (InvocationTargetException e) {
            return resp.setSuccess(false).setThrowable(e.getTargetException());
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
