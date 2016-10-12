package mobilecloud.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
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
    
    private static Context context;
    private static Server instance;
    
    private final Map<String, Handler> handlers;
    
    public Server(ExecutableLoader executableLoader) {
        this.handlers = new ConcurrentHashMap<>();
       
        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler(executableLoader));
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableHandler(executableLoader));
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
     * Initialize this server
     * @param context server application context
     */
    public static void init(Context context) {
        Server.context = context;
        Engine.cloudInit();
    }
    
    /**
     * Get singleton server instance
     * @return the server
     */
    public static Server getInstance() {
        if(instance == null) {
            synchronized(Server.class) {
                if(instance == null) {
                    instance = new Server(new ExecutableLoader(context));
                }
            }
        }
        return instance;
    }

}
