package mobilecloud.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.request.UploadApplicationExecutableRequest;
import mobilecloud.api.response.Response;
import mobilecloud.api.deliverer.Deliverer;
import mobilecloud.api.deliverer.GetAvailableServerRequestDeliverer;
import mobilecloud.api.deliverer.MonitorHostRequestDeliverer;
import mobilecloud.api.deliverer.RegisterServerRequestDeliverer;
import mobilecloud.api.deliverer.RemoteInvocationRequestDeliverer;
import mobilecloud.api.deliverer.UploadApplicationExecutableRequestDeliverer;
import mobilecloud.engine.host.Host;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

/**
 * A client which interacts with server
 *
 */
public class Client {
    
    private static Client instance;
    
    private final SocketBuilder builder;
    private final TimerKeyLock lock;
    private final int conenctionTimeOut;
    private final Map<String, Deliverer> deliverers;
    
    public Client(SocketBuilder builder, int minRequestInterval, int conenctionTimeOut) {
        this.builder = builder;
        this.lock = new TimerKeyLock(minRequestInterval);
        this.conenctionTimeOut = conenctionTimeOut;
        this.deliverers = new ConcurrentHashMap<>();
        
        this.registerDeliverer(MonitorHostRequest.class.getName(), new MonitorHostRequestDeliverer());
        this.registerDeliverer(RemoteInvocationRequest.class.getName(), new RemoteInvocationRequestDeliverer());
        this.registerDeliverer(UploadApplicationExecutableRequest.class.getName(),
                new UploadApplicationExecutableRequestDeliverer());
        this.registerDeliverer(RegisterServerRequest.class.getName(), new RegisterServerRequestDeliverer());
        this.registerDeliverer(GetAvailableServerRequest.class.getName(), new GetAvailableServerRequestDeliverer());
    }
    
    /**
     * Register a deliverer for a specific request type
     * @param requestName the request name
     * @param deliverer the deliverer to deliver request
     * @return this client
     */
    public Client registerDeliverer(String requestName, Deliverer deliverer) {
        this.deliverers.put(requestName, deliverer);
        return this;
    }
    
    /**
     * Send a request to the server and waits for response
     * @param request the request
     * @return the response
     * @throws Exception if any problem occurs during the request
     */
    public Response request(Request request) throws Exception {
        Deliverer deliverer = deliverers.get(request.getClass().getName());
        if (deliverer == null) {
            throw new IllegalArgumentException("No deliverer to handle " + request.getClass().getName());
        }
        
        Host host = new Host(request.getIp(), request.getPort());

        // Request lock before connecting to a host. This controls the frequency of requests
        lock.lock(host);
        Socket socket = null;
        try {
            socket = builder.build(request.getIp(), request.getPort(), conenctionTimeOut);
        } finally {
            lock.unlock(host);
        }
        
        try {
            ObjectInputStreamWrapper is = new ObjectInputStreamWrapper(
                    new BufferedInputStream(socket.getInputStream()));
            ObjectOutputStreamWrapper os = new ObjectOutputStreamWrapper(
                    new BufferedOutputStream(socket.getOutputStream()));
            
            //Firstly send request type
            os.get().writeObject(request.getClass().getName());
            
            //Then deliver request data
            deliverer.deliver(request, is, os);
            
            //Get response
            return (Response) is.get().readObject();
        } finally {
            socket.close();
        }
    }
    
    /**
     * Get the singleton instance of client
     * @return the client instance
     */
    public static Client getInstance() {
        if(instance == null) {
            synchronized(Client.class) {
                if(instance == null) {
                    instance = new Client(new DefaultSocketBuilder(), Config.MIN_REQUEST_INTERVAL, Config.CONNECTION_TIME_OUT);
                }
            }
        }
        return instance;
    }

}
