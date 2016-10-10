package mobilecloud.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.engine.host.Host;

/**
 * A client which interacts with server
 *
 */
public class Client {
    
    private static Client instance;
    
    private final SocketBuilder builder;
    private final TimerKeyLock lock;
    private final int conenctionTimeOut;
    
    public Client(SocketBuilder builder, int minRequestInterval, int conenctionTimeOut) {
        this.builder = builder;
        this.lock = new TimerKeyLock(minRequestInterval);
        this.conenctionTimeOut = conenctionTimeOut;
    }
    
    /**
     * Send a request to the server and waits for response
     * @param request the request
     * @return the response
     * @throws Exception if any problem occurs during the request
     */
    public Response request(Request request) throws Exception {
        Host host = new Host(request.getIp(), request.getPort());
        lock.lock(host);
        Socket socket = null;
        try {
            socket = builder.build(request.getIp(), request.getPort(), conenctionTimeOut);
        } finally {
            lock.unlock(host);
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            os.writeObject(request);
            os.flush();
            return (Response) is.readObject();
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
