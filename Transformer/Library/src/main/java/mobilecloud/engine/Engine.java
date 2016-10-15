package mobilecloud.engine;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import android.content.Context;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.RemoteInvocationResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.client.Client;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.monitor.HostMonitor;
import mobilecloud.engine.host.monitor.HostStatusChangeListener;
import mobilecloud.engine.schedular.Schedular;
import mobilecloud.lib.RemoteExecutionListener;
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.Token;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.server.NoApplicationExecutableException;

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
     * Indicate that current environment is on cloud. Cloud environment should
     * invoke this method before first execution.
     */
    public static void cloudInit() {
        onCloud = true;
    }

    /**
     * Initialize local environment with Android Context and a HostMonitor
     * 
     * @param ctxt
     *            the Android application context
     * @param monitor
     *            the Monitor which is used to monitor server hosts
     */
    public static void localInit(Context ctxt) {
        onCloud = false;
        context = ctxt;
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
     * Indicate current host
     */
    private static class LocalHost extends Host {
        public LocalHost() {
            super("localHost", 0);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != LocalHost.class) {
                return false;
            } else {
                LocalHost that = (LocalHost) o;
                Host me = new Host(ip, port);
                Host he = new Host(that.ip, that.port);
                return me.equals(he);
            }
        }
    }

    private final Context ctx;
    private final Client client;
    private final Schedular schedular;
    private final ExecutableProvider executableProvider;
    private final HostMonitor hostMonitor;
    
    public Engine() {
        if(!isOnCloud()) {
            throw new IllegalStateException("This constructor is only for cloud environment");
        }
        this.ctx = null;
        this.client = null;
        this.schedular = null;
        this.executableProvider = null;
        this.hostMonitor = null;
    }

    public Engine(Context context, Client client, Schedular schedular, ExecutableProvider executableProvider, HostMonitor hostMonitor) {
        if(isOnCloud()) {
            throw new IllegalStateException("This constructor is only for local environment.");
        }
        this.ctx = context;
        this.schedular = schedular;
        this.client = client;
        this.executableProvider = executableProvider;
        this.hostMonitor = hostMonitor;

        // Start running host monitor
        this.hostMonitor.withHostStatusChangeListener(new HostStatusChangeListener() {
            @Override
            public void onHostStatusChange(Host host, boolean isAlive) {
                if(isAlive) {
                    Engine.this.schedular.addHost(host);
                } else {
                    Engine.this.schedular.removeHost(host);
                }
                
            }
        }).start();
        
        // Add local host to this schedular, so that schedular can schedule
        // local host to invoke a method.
        this.schedular.addHost(new LocalHost());
    }

    /**
     * Determine if an invocation should be migrated to the cloud at current
     * point. 
     * @param method the method to be invoked
     * @param invoker the invoker
     * @param args the arguments to be passed
     * @return true if it should be migrated
     */
    public boolean shouldMigrate(Method method, Object invoker, Object... args) {
        if (method == null || isOnCloud() || !schedular.haveAvailable()) {
            return false;
        }
        if (Modifier.isNative(method.getModifiers())) {
            return false;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (!(arg instanceof Serializable)) {
                return false;
            }
            if (arg instanceof Context) {
                return false;
            }
        }
        if (invoker != null && (!(invoker instanceof Serializable) || invoker instanceof Context)) {
            return false;
        }
        return true;
    }
    
    /**
     * Get the current applciation name
     * 
     * @return the app name
     */
    public String appName() {
        return ctx.getPackageName();
    }

    /**
     * Invoke this method on cloud
     * 
     * @param listener
     *            the remote execution listener to listen to execution events
     * @param method
     *            the method to be invoked
     * @param invoker
     *            the invoker against this method
     * @param args
     *            the arguments to be passed
     * @return the result of this execution
     * @throws RemoteExecutionAbortedException 
     *             if the execution is aborted manually or by schedular
     * @throws RemoteExecutionFailedException
     *             if remote execution fails due to any reason
     */
    public Object invokeRemotely(RemoteExecutionListener listener, Method method, Object invoker, Object... args) {

        // Check available hosts
        Host host = schedular.schedule();
        if (host == null) {
            throw new RemoteExecutionAbortedException("No host available!");
        } else if (host instanceof LocalHost) {
            throw new RemoteExecutionAbortedException("Shedular schedule to run this method locally!");
        }
        
        if(!listener.onRemoteExecutionStart(method, invoker, args)) {
            throw new RemoteExecutionAbortedException("Execution is aborted manually");
        }
        
        try {

            // Record object meta data
            ObjectMigrator migrator = new ObjectMigrator();
            migrator.migrate(invoker);
            for (Object arg : args) {
                migrator.migrate(arg);
            }

            // Build a request
            RemoteInvocationRequest request = buildInvocationRequest(host.ip, host.port, method, invoker, args,
                    migrator.takeToken().expand());

            // Send the request
            Response resp = null;
            try {
                resp = client.request(request);
            } catch (NoApplicationExecutableException e) {
                // If server does not have executable files, send it to server

                uploadExecutable(host); // Upload apk file

                resp = client.request(request); // Retry this request
            }

            // If execution fails, throw an exception
            if (!resp.isSuccess()) {
                if (resp.getThrowable() != null) {
                    throw resp.getThrowable();
                } else {
                    throw new UnknownErrorException();
                }
            }

            // Synchronize objects
            RemoteInvocationResponse invocResp = (RemoteInvocationResponse) resp;
            migrator.sync(invocResp.getToken(), invocResp.getDiffs());

            Object res = migrator.getObject(invocResp.getReturnVal());
            listener.onRemoteExecutionComplete(method, invoker, args, res, true, null);
            
            return res;
        } catch (Throwable t) {
            listener.onRemoteExecutionComplete(method, invoker, args, null, false, t);
            throw new RemoteExecutionFailedException("Remote execution fails!", t);
        }
    }

    // Construct a remote invocation request
    private RemoteInvocationRequest buildInvocationRequest(String ip, int port, Method method, Object invoker,
            Object[] args, Token token) throws IOException {
        RemoteInvocationRequest request = new RemoteInvocationRequest();
        request.setApplicationId(appName()).setClazzName(method.getDeclaringClass().getName())
                .setMethodName(method.getName()).setToken(token).setInvoker(invoker).setArgs(args).setIp(ip)
                .setPort(port);
        Class<?>[] params = method.getParameterTypes();
        String[] argTypes = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            argTypes[i] = params[i].getName();
        }
        request.setArgTypesName(argTypes);
        return request;
    }
    
    //Upload executable file to a host
    private void uploadExecutable(Host host) throws Throwable {
        Request req = new UploadApplicationExecutableRequest().setApplicationId(appName())
                .setExecutablePath(executableProvider.provide()).setIp(host.ip)
                .setPort(host.port);
        try {
            Response resp = client.request(req);
            if (!resp.isSuccess()) {
                throw resp.getThrowable();
            }
        } catch (DuplicateExecutableException e) {}
    }

    /**
     * Get the singleton instance of Engine
     * 
     * @return The engine instance
     */
    public static Engine getInstance() {
        if (engine == null) {
            synchronized (Engine.class) {
                if (engine == null) {
                    if(isOnCloud()) {
                        engine = new Engine();
                    } else {
                        engine = new Engine(context, Client.getInstance(), Schedular.getInstance(),
                                new ExecutableProvider(context), HostMonitor.getInstance());
                    }
                }
            }
        }
        return engine;
    }

}