package mobilecloud.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.request.UploadApplicationExecutableRequest;
import mobilecloud.api.response.IllegalRequestResponse;
import mobilecloud.api.response.InternalServerErrorResponse;
import mobilecloud.api.response.Response;
import mobilecloud.engine.Engine;
import mobilecloud.metric.Metric;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.handler.Handler;
import mobilecloud.server.handler.invocation.RemoteInvocationHandler;
import mobilecloud.server.handler.monitorhost.MonitorHostRequestHandler;
import mobilecloud.server.handler.upload.UploadApplicationExecutableHandler;
import mobilecloud.api.deliverer.Deliverer;
import mobilecloud.api.deliverer.IllegalRequestResponseDeliverer;
import mobilecloud.api.deliverer.InternalServerErrorResponseDeliverer;
import mobilecloud.api.deliverer.MonitorHostResponseDeliverer;
import mobilecloud.api.deliverer.RemoteInvocationResponseDeliverer;
import mobilecloud.api.deliverer.UploadApplicationExecutableResponseDeliverer;
import mobilecloud.api.receiver.MonitorHostRequestReceiver;
import mobilecloud.api.receiver.Receiver;
import mobilecloud.api.receiver.RemoteInvocationRequestReceiver;
import mobilecloud.api.receiver.UploadApplicationExecutableRequestReceiver;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

/**
 * A server object
 */
public class Server {
    
    private static Context context;
    private static Server instance;
    
    private final Map<String, Handler> handlers;
    private final Map<String, Receiver<Request>> receivers;
    private final Map<String, Deliverer<Response>> deliverers;
    private final MetricGenerator metricGenerator;
    
    public Server(ExecutableLoader executableLoader, MetricGenerator metricGenerator) {
        this.handlers = new ConcurrentHashMap<>();
        this.receivers = new ConcurrentHashMap<>();
        this.deliverers = new ConcurrentHashMap<>();
        this.metricGenerator = metricGenerator;

        this.registerHandler(RemoteInvocationRequest.class.getName(), new RemoteInvocationHandler(executableLoader));
        this.registerHandler(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableHandler(executableLoader));
        this.registerHandler(MonitorHostRequest.class.getName(), new MonitorHostRequestHandler(metricGenerator));

        this.registerReceiver(RemoteInvocationRequest.class.getName(),
                new RemoteInvocationRequestReceiver(executableLoader, metricGenerator));
        this.registerReceiver(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableRequestReceiver(executableLoader, metricGenerator));
        this.registerReceiver(MonitorHostRequest.class.getName(), new MonitorHostRequestReceiver(metricGenerator));

        this.registerDeliverer(IllegalRequestResponseDeliverer.class.getName(), new IllegalRequestResponseDeliverer(metricGenerator));
        this.registerDeliverer(InternalServerErrorResponseDeliverer.class.getName(), new InternalServerErrorResponseDeliverer(metricGenerator));
        this.registerDeliverer(MonitorHostResponseDeliverer.class.getName(), new MonitorHostResponseDeliverer(metricGenerator));
        this.registerDeliverer(RemoteInvocationResponseDeliverer.class.getName(), new RemoteInvocationResponseDeliverer(metricGenerator));
        this.registerDeliverer(UploadApplicationExecutableResponseDeliverer.class.getName(), new UploadApplicationExecutableResponseDeliverer(metricGenerator));
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
    public Server registerReceiver(String type, Receiver<Request> receiver) {
        this.receivers.put(type, receiver);
        return this;
    }
    
    /**
     * Register a deliverer to this server
     * @param type the type of response
     * @param deliverer the deliverer to deliver a response
     * @return this server
     */
    public Server registerDeliverer(String type, Deliverer<Response> deliverer) {
        this.deliverers.put(type, deliverer);
        return this;
    }
    
    /**
     * Get the current metric of this server
     * @return current metric
     */
    public Metric getMetric() {
        return metricGenerator.getMetric();
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
     * @param listener the listener to monitor serving process. Could be null.
     * @throws Exception if error happens
     */
    public void serve(InputStream is, OutputStream os, ServerListener listener) throws Exception {
        AdvancedObjectInputStreamWrapper in = new AdvancedObjectInputStreamWrapper(new BufferedInputStream(is));
        AdvancedObjectOutputStreamWrapper out = new AdvancedObjectOutputStreamWrapper(new BufferedOutputStream(os));

        // Read type of request
        String type = (String) in.get().readObject();

        // Get receiver
        Receiver<Request> receiver = receivers.get(type);
        if (receiver == null) {
            throw new IllegalRequestException(type);
        }

        if(listener != null) {
            listener.onRequestReceivingStarts(type);
        }
        Request req = receiver.receive(in, out);
        if(listener != null) {
            listener.onRequestReceived(req);
        }
        
        Response resp = serve(req);
        Deliverer<Response> deliverer = deliverers.get(resp.getClass().getName());
        if (deliverer == null) {
            throw new IllegalRequestException(resp.getClass().getName());
        }
        
        out.get().writeObject(resp.getClass().getName());
        deliverer.deliver(resp, in, out);
        
        if(listener != null) {
            listener.onResponseSent(req, resp);
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
                    instance = new Server(new ExecutableLoader(context), MetricGenerator.getInstance());
                }
            }
        }
        return instance;
    }

}
