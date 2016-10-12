package mobilecloud.engine;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import android.content.Context;
import mobilecloud.api.Invocation;
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
import mobilecloud.objs.ObjectMigrator;
import mobilecloud.objs.Token;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.handler.upload.DuplicateExecutableException;
import mobilecloud.utils.ByteProvider;
import mobilecloud.utils.IOUtils;

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
    public static void localInit(Context ctxt, HostMonitor monitor) {
        onCloud = false;
        context = ctxt;
        if(monitor != null) {
            //monitor is provided, start the monitor
            monitor.withHostStatusChangeListener(new HostStatusChangeListener() {
                @Override
                public void onHostStatusChange(Host host, boolean isAlive) {
                    if (isAlive) {
                        Schedular.getInstance().addHost(host);
                    } else {
                        Schedular.getInstance().removeHost(host);
                    }
                }
            }).start();
        }
    }

    /**
     * Initialize local environment with Android Context and the default host
     * monitor
     * 
     * @param ctxt
     *            the Android application context
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
    private final ByteProvider executableProvider;

    public Engine(Context context, Client client, Schedular schedular, ByteProvider executableProvider) {
        this.ctx = context;
        this.schedular = schedular;
        this.client = client;
        this.executableProvider = executableProvider;

        // Add local host to this schedular, so that schedular can schedule
        // local host to invoke a method.
        this.schedular.addHost(new LocalHost());
    }

    /**
     * Determine if an invocation should be migrated to the cloud
     * 
     * @return true if should migrate
     */
    public boolean shouldMigrate(Method method, Object invoker, Object... args) {
        if (method == null || isOnCloud() || !schedular.haveAvailable()) {
            return false;
        }
        for (Object arg : args) {
            if (arg != null && !(arg instanceof Serializable)) {
                return false;
            }
        }
        if (invoker != null && !(invoker instanceof Serializable)) {
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
     * @return the result of this execution
     * @throws Exception
     *             if execution fails
     */
    public Object invokeRemotely(Method method, Object invoker, Object... args) {
        // Check available hosts
        Host host = schedular.schedule();
        if (host == null) {
            throw new IllegalStateException("No host available!");
        }

        // Record object meta data
        ObjectMigrator migrator = new ObjectMigrator();
        migrator.migrate(invoker);
        for (Object arg : args) {
            migrator.migrate(arg);
        }

        try {
            // Build a request
            RemoteInvocationRequest request = buildInvocationRequest(host.ip, host.port, method, invoker, args,
                    migrator.takeToken().expand());

            // Send the request
            Response resp = client.request(request);

            // If server does not have executable files, send it to server
            if (!resp.isSuccess() && (resp.getThrowable() instanceof NoApplicationExecutableException)) {

                // Upload apk file
                uploadAPK(host);

                // Retry this request
                resp = client.request(request);
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
            
            return migrator.getObject(invocResp.getReturnVal());
        } catch (Throwable t) {
            throw new RemoteExecutionFailedException("Remote execution fails!", t);
        }
    }

    // Construct a remote invocation request
    private RemoteInvocationRequest buildInvocationRequest(String ip, int port, Method method, Object invoker,
            Object[] args, Token token) throws IOException {
        RemoteInvocationRequest request = new RemoteInvocationRequest();
        request.setApplicationId(appName()).setClazzName(method.getDeclaringClass().getName())
                .setMethodName(method.getName()).setIp(ip).setPort(port);
        Class<?>[] params = method.getParameterTypes();
        String[] argTypes = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            argTypes[i] = params[i].getName();
        }
        Invocation invocation = new Invocation().setToken(token).setInvoker(invoker).setArgs(args);
        request.setArgTypesName(argTypes).setInvocationData(IOUtils.toBytesArray(invocation));
        return request;
    }
    
    //Upload apk file to a host
    private void uploadAPK(Host host) throws Throwable {
        Request req = new UploadApplicationExecutableRequest().setApplicationId(appName())
                .setExecutable(executableProvider.provide()).setIp(host.ip).setPort(host.port);
        Response resp = client.request(req);
        if (!resp.isSuccess() && !(resp.getThrowable() instanceof DuplicateExecutableException)) {
            throw resp.getThrowable();
        }
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
                    engine = new Engine(context, Client.getInstance(), Schedular.getInstance(), new ExecutableByteProvider(context));
                }
            }
        }
        return engine;
    }

}