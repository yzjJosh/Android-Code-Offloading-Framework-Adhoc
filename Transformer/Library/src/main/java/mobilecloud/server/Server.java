package mobilecloud.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import mobilecloud.invocation.RemoteInvocationHandler;
import mobilecloud.invocation.RemoteInvocationRequest;
import mobilecloud.upload.UploadApplicationExecutableHandler;
import mobilecloud.upload.UploadApplicationExecutableRequest;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

public class Server {
    
    private static Server instance;
    
    private Map<String, Handler> handlers;
    private Map<String, ClassLoader> classLoaders;
    
    private Server() {
        this.handlers = new ConcurrentHashMap<>();
        this.classLoaders = new ConcurrentHashMap<>();
        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler());
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(), new UploadApplicationExecutableHandler());
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
    public Server registerClassLoader(String applicationId, @NonNull ClassLoader cl) {
        this.classLoaders.put(applicationId, cl);
        return this;
    }
    
    /**
     * Get a class loader belongs to an application
     * @param applicationId the application id to retrieve
     * @return the class loader
     */
    public ClassLoader getClassLoader(String applicationId) {
        return this.classLoaders.get(applicationId);
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
