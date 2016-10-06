package mobilecloud.engine;

import java.io.Serializable;
import java.lang.reflect.Method;

import android.content.Context;
import lombok.NonNull;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Response;
import mobilecloud.client.Client;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.monitor.HostMonitor;
import mobilecloud.engine.host.monitor.HostStatusChangeListener;
import mobilecloud.engine.schedular.Schedular;

/**
 * The cloud compute engine
 */
public class Engine {
    
    // Singleton instance
    private static Engine engine;
    
    // A flag to indicate if current environment is on cloud or not
    private static boolean onCloud;
    
    // The Android application context
    private static Context context;
    
    /**
     * Indicate that current environment is on cloud. Cloud environment should invoke 
     * this method before first execution.
     */
    public static void cloudInit() {
        onCloud = true;
    }
    
    /**
     * Initialize local environment with Android Context and a HostMonitor
     * @param ctxt the Android application context
     * @param monitor the Monitor which is used to monitor server hosts
     */
    public static void localInit(@NonNull Context ctxt, @NonNull HostMonitor monitor) {
        onCloud = false;
        context = ctxt;
        monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                if(isAlive) {
                    Schedular.getInstance().addHost(host);
                } else {
                    Schedular.getInstance().removeHost(host);
                }
            }
        }).start();
    }
    
    /**
     * Initialize local environment with Android Context and the default host monitor
     * @param ctxt the Android application context
     */
    public static void localInit(Context ctxt) {
        localInit(ctxt, HostMonitor.getInstance());
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
     * Get the current applciation name
     * @return the app name
     */
    public static String appName() {
        if (context == null) {
            throw new NullPointerException("No context available! Please init engine with available context!");
        } else {
            return context.getPackageName();
        }
    }
    
    /**
     * Indicate current host
     */
    private static class LocalHost extends Host {
        public LocalHost() {
            super("localHost", 0);
        }
    }
    
    

    private final Schedular schedular;
    private final Client client;
    
    public Engine(@NonNull Schedular schedular, @NonNull Client client) {
        this.schedular = schedular;
        this.client = client;
        
        // Add local host to this schedular, so that schedular can schedule
        // local host to invoke a method.
        this.schedular.addHost(new LocalHost());
    }
    
    /**
     * Determine if an invocation should be migrated to the cloud
     * @return true if should migrate
     */
    public boolean shouldMigrate(Method method, Object invoker, Object... args) {
        if(isOnCloud() || !schedular.haveAvailable()) {
            return false;
        }
        for(Object arg: args) {
            if(!(arg instanceof Serializable)) {
                return false;
            }
        }
        if(!(invoker instanceof Serializable)) {
            return false;
        }
        if (schedular.trySchedule() instanceof LocalHost) {
            // If the next host to be scheduled is local host, we should do
            // computation locally, thus return false.
            schedular.schedule();
            return false;
        }
        return true;
    }
    
    /**
     * Invoke this method on cloud
     * @return the result of this execution
     * @throws Exception if execution fails
     */
    public Object invokeRemotely(Method method, Object invoker, Object... args) {
        // Check available hosts
        Host host = schedular.schedule();
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
            Response resp = client.request(request);

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
        request.setApplicationId(appName()).setClazzName(method.getDeclaringClass().getName())
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
                    engine = new Engine(Schedular.getInstance(), Client.getInstance());
                }
            }
        }
        return engine;
    }
    
}