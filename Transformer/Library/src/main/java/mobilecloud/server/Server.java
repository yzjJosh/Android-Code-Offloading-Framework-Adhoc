package mobilecloud.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import mobilecloud.api.IllegalRequestResponse;
import mobilecloud.api.InternalServerErrorResponse;
import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.RemoteInvocationRequest;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.server.handler.Handler;
import mobilecloud.server.handler.invocation.RemoteInvocationHandler;
import mobilecloud.server.handler.monitorhost.MonitorHostRequestHandler;
import mobilecloud.server.handler.upload.UploadApplicationExecutableHandler;

/**
 * A server object
 */
public class Server {
    
    private static Server instance;
    
    private Map<String, Handler> handlers;
    private Map<String, ClassLoader> classLoaders;
    
    public Server() {
        this.handlers = new ConcurrentHashMap<>();
        this.classLoaders = new ConcurrentHashMap<>();
        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler(this));
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(), new UploadApplicationExecutableHandler(this));
        this.registerHandler(MonitorHostRequest.class.getName(), new MonitorHostRequestHandler());
    }
    
    /**
     * Register a specific handler to this server
     * @param type the type of request
     * @param handler the handler to handle this request
     * @return this server
     */
    public Server registerHandler(@NonNull String type, @NonNull Handler handler) {
        this.handlers.put(type, handler);
        return this;
    }
    
    /**
     * Register a class loader which contains classes for a specific application
     * @param applicationId the application id
     * @param cl the class loader for that application
     * @return this server
     */
    public Server registerClassLoader(@NonNull String applicationId, @NonNull ClassLoader cl) {
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
                cl = new APKLoader().loadAPK(applicationId);
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
    public Response serve(@NonNull Request request) {
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
     * Get singleton server instance
     * @return the server
     */
    public static Server getInstance() {
        if(instance == null) {
            synchronized(Server.class) {
                if(instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }

}
