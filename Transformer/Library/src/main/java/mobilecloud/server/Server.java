package mobilecloud.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.api.IllegalRequestResponse;
import mobilecloud.api.InternalServerErrorResponse;
import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.engine.Engine;
import mobilecloud.server.handler.Handler;
import mobilecloud.server.handler.invocation.RemoteInvocationHandler;
import mobilecloud.server.handler.monitorhost.MonitorHostRequestHandler;
import mobilecloud.server.handler.upload.UploadApplicationExecutableHandler;

/**
 * A server object
 */
public class Server {
    
    private final APKLoader apkLoader;
    private final Map<String, Handler> handlers;
    private final Map<String, ClassLoader> classLoaders;
    
    public Server(APKLoader apkLoader) {
        this.apkLoader = apkLoader;
        this.handlers = new ConcurrentHashMap<>();
        this.classLoaders = new ConcurrentHashMap<>();
        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler(this));
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableHandler(this, apkLoader));
        this.registerHandler(MonitorHostRequest.class.getName(), new MonitorHostRequestHandler());
    }
    
    /**
     * Register a specific handler to this server
     * @param type the type of request
     * @param handler the handler to handle this request
     * @return this server
     */
    public Server registerHandler(String type, Handler handler) {
        this.handlers.put(type, handler);
        return this;
    }
    
    /**
     * Register a class loader which contains classes for a specific application
     * @param applicationId the application id
     * @param cl the class loader for that application
     * @return this server
     */
    public Server registerClassLoader(String applicationId, ClassLoader cl) {
        this.classLoaders.put(applicationId, cl);
        return this;
    }
    
    /**
     * Get a class loader belongs to an application
     * @param applicationId the application id to retrieve
     * @return the class loader
     */
    public ClassLoader getClassLoader(String applicationId) {
        ClassLoader cl = classLoaders.get(applicationId);
        if(cl == null) {
            try {
                cl = apkLoader.loadAPK(applicationId);
                registerClassLoader(applicationId, cl);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return cl;
    }
    
    /**
     * Serve a request
     * @param request the request to serve
     * @return the response to this request
     */
    public Response serve(Request request) {
        Handler handler = handlers.get(request.getClass().getName());
        if(handler == null) {
            return new IllegalRequestResponse(new IllegalRequestException(request.getClass().getName()));
        }
        try{
            return handler.handle(request);
        } catch(Exception e) {
            e.printStackTrace();
            return new InternalServerErrorResponse(new InternalServerError());
        }
    }
    
    /**
     * Initialize server
     * @param context server application context
     */
    public static void init() {
        Engine.cloudInit();
    }

}
