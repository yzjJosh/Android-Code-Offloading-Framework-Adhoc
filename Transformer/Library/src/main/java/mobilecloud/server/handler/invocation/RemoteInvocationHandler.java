package mobilecloud.server.handler.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.ClassUtils;

/**
 * A class which handles remote invocation requests
 */
public class RemoteInvocationHandler implements Handler {
    
    private Server server;
    
    public RemoteInvocationHandler(Server server) {
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
            String[] argTypesName = invocReq.getArgTypesName();
            List<byte[]> argsData = invocReq.getArgsData();
            byte[] invokerData = invocReq.getInvokerData();
            
            Class<?> declareClazz = ClassUtils.loadClass(loader, invocReq.getClazzName());
            Class<?>[] argTypes = new Class<?>[argTypesName.length];
            Object[] args = new Object[argTypesName.length];
            for (int i = 0; i < argTypesName.length; i++) {
                argTypes[i] = ClassUtils.loadClass(loader, argTypesName[i]);
                args[i] = ClassUtils.readObject(argsData.get(i), loader);
            }
            Object invoker = ClassUtils.readObject(invokerData, loader);
            
            // Invoke the method
            Method method = declareClazz.getDeclaredMethod(invocReq.getMethodName(), argTypes);
            method.setAccessible(true);
            Object ret = method.invoke(invoker, args);
            
            //If invoker or parameters are not changed, we ignore them in response
            byte[] newInvokerData = ClassUtils.toBytesArray(invoker);
            if(Arrays.equals(invokerData, newInvokerData)) {
                invokerData = null;
            } else {
                invokerData = newInvokerData;
            }
            for(int i=0; i < args.length; i++) {
                byte[] newArgData = ClassUtils.toBytesArray(args[i]);
                if(Arrays.equals(argsData.get(i), newArgData)) {
                    argsData.set(i, null);
                } else {
                    argsData.set(i, newArgData);
                }
            }
            
            resp.setArgsData(argsData).setInvokerData(invokerData).setReturnValueData(ClassUtils.toBytesArray(ret))
                    .setSuccess(true);
            return resp;
        } catch (InvocationTargetException e) {
            return resp.setSuccess(false).setThrowable(e.getTargetException());
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
