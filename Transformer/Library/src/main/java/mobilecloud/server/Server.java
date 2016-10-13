package mobilecloud.server;

import java.io.InputStream;
import java.io.OutputStream;
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
import mobilecloud.server.receiver.MonitorHostRequestReceiver;
import mobilecloud.server.receiver.Receiver;
import mobilecloud.server.receiver.RemoteInvocationRequestReceiver;
import mobilecloud.server.receiver.UploadApplicationExecutableRequestReceiver;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

/**
 * A server object
 */
public class Server {
    
    private static Context context;
    private static Server instance;
    
    private final Map<String, Handler> handlers;
    private final Map<String, Receiver> receivers;
    
    public Server(ExecutableLoader executableLoader) {
        this.handlers = new ConcurrentHashMap<>();
        this.receivers = new ConcurrentHashMap<>();

        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler(executableLoader));
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableHandler(executableLoader));
        this.registerHandler(MonitorHostRequest.class.getName(), new MonitorHostRequestHandler());

        this.registerReceiver(RemoteInvocationRequest.class.getName(),
                new RemoteInvocationRequestReceiver(executableLoader));
        this.registerReceiver(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableRequestReceiver());
        this.registerReceiver(MonitorHostRequest.class.getName(), new MonitorHostRequestReceiver());
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
     * Register a receiver to this server
     * @param type the type of request
     * @param receiver the receiver to receive a request
     * @return this server
     */
    public Server registerReceiver(String type, Receiver receiver) {
        this.receivers.put(type, receiver);
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
     * Read request from an input stream and write resposne to the output stream
     * @param is the input stream to read request
     * @param os the output stream to write resposne
     * @return the response written to output stream
     * @throws Exception if error happens
     */
    public Response serve(InputStream is, OutputStream os) throws Exception {
        ObjectInputStreamWrapper in = new AdvancedObjectInputStreamWrapper(is);
        ObjectOutputStreamWrapper out = new ObjectOutputStreamWrapper(os);

        // Read type of request
        String type = (String) in.get().readObject();

        // Get receiver
        Receiver receiver = receivers.get(type);
        if (receiver == null) {
            Response resp = new IllegalRequestResponse(new IllegalRequestException(type));
            out.get().writeObject(resp);
            out.get().flush();
            return resp;
        }

        Request req = receiver.receive(in, out);
        
        Response resp = serve(req);
        out.get().writeObject(resp);
        out.get().flush();
        return resp;
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
