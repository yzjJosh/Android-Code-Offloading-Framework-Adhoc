package mobilecloud.engine;

import java.io.Serializable;
import java.lang.reflect.Method;

import lombok.Getter;
import lombok.Setter;
import mobilecloud.client.Client;
import mobilecloud.invocation.RemoteInvocationRequest;
import mobilecloud.invocation.RemoteInvocationResponse;
import mobilecloud.utils.Response;

/**
 * The cloud compute engine
 */
public class Engine {
    
    //Singleton instance
    private static Engine engine;
    
    //A flag to indicate if current environment is on cloud or not
    private static boolean onCloud = false;
    
    @Getter
    @Setter
    private String AppName;
    
    
    /**
     * Indicate that current environment is on cloud. Cloud environment should invoke 
     * this method before first execution.
     */
    public static void cloudInit() {
        onCloud = true;
    }
    
    /**
     * Test if current environment is on cloud or not
     * 
     * @return if current environment is on cloud
     */
    public static boolean isOnCloud() {
        return onCloud;
    }
    
    /**
     * Determine if an invocation should be migrated to the cloud
     * @return true if should migrate
     */
    public boolean shouldMigrate(Method method, Object invoker, Object... args) {
        if(onCloud || !Schedular.getInstance().haveAvailable()) {
            return false;
        }
        if(!(invoker instanceof Serializable)) {
            return false;
        }
        for(Object arg: args) {
            if(!(arg instanceof Serializable)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Invoke this method on cloud
     * @return the result of this execution
     * @throws Exception if execution fails
     */
    public Object invokeRemotely(Method method, Object invoker, Object... args) {
        if (this.getAppName() == null) {
            throw new IllegalStateException("No app name found!");
        }

        // Check available hosts
        Host host = Schedular.getInstance().schedule();
        if (host == null) {
            throw new IllegalStateException("No host available!");
        }

        // Build a request
        RemoteInvocationRequest request = buildInvocationRequest(host.ip, host.port, method, invoker, args);

        // Record object meta data
        ObjectMigrator migrator = new ObjectMigrator();
        migrator.moveOut(invoker);
        for (Object arg : args) {
            migrator.moveOut(arg);
        }

        try {
            // Send the request
            Response resp = Client.getInstance().request(request);

            // Synchronize objects firstly
            if (resp instanceof RemoteInvocationResponse) {
                RemoteInvocationResponse invocResp = (RemoteInvocationResponse) resp;
                migrator.sync(invocResp.getInvoker());
                for (Object arg : invocResp.getArgs()) {
                    migrator.sync(arg);
                }
            }

            // If execution fails, throw an exception
            if (!resp.isSuccess()) {
                if (resp.getThrowable() != null) {
                    throw resp.getThrowable();
                } else {
                    throw new UnknownErrorException();
                }
            }

            // Sync return value and return it
            RemoteInvocationResponse invocResp = (RemoteInvocationResponse) resp;
            return migrator.sync(invocResp.getReturnValue());
        } catch (Throwable t) {
            throw new RemoteExecutionFailedException("Caused by: " + t).withReaseon(t);
        } finally {
            // In case of exception, put back objects
            migrator.joinObjects();
        }
    }
    
    // Construct a remote invocation request
    private RemoteInvocationRequest buildInvocationRequest(String ip, int port, Method method, Object invoker,
            Object[] args) {
        RemoteInvocationRequest request = new RemoteInvocationRequest();
        request.setApplicationId(getAppName()).setClazzName(method.getDeclaringClass().getName())
                .setMethodName(method.getName()).setInvoker((Serializable)invoker).setIp(ip).setPort(port);
        Class<?>[] params = method.getParameterTypes();
        String[] argTypes = new String[params.length];
        Serializable[] arguments = new Serializable[params.length];
        for (int i = 0; i < params.length; i++) {
            argTypes[i] = params[i].getName();
            arguments[i] = (Serializable) args[i];
        }
        request.setArgTypesName(argTypes).setArgs(arguments);
        return request;
    }
    
    /**
     * Get the singleton instance of Engine
     * @return The engine instance
     */
    public static Engine getInstance() {
        if(engine == null) {
            synchronized(Engine.class) {
                if(engine == null) {
                    engine = new Engine();
                }
            }
        }
        return engine;
    }
    
}
