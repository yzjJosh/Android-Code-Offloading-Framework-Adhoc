package mobilecloud.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lombok.NonNull;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

/**
 * A client which interacts with server
 *
 */
public class Client {
    
    private static Client instance;
    
    private SocketBuilder builder = new DefaultSocketBuilder();
    
    /**
     * Send a request to the server and waits for response
     * @param request the request
     * @return the response
     * @throws Exception if any problem occurs during the request
     */
    public Response request(@NonNull Request request) throws Exception {
        Socket socket = builder.build(request.getIp(), request.getPort());
        try {
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeObject(request);
            os.flush();
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            return (Response) is.readObject();
        } finally {
            socket.close();
        }
    }
    
    /**
     * Set the socket builder of this client. This method is for testing purpose.
     * @param builder the builder to set
     */
    public void setSocketBuilder(SocketBuilder builder) {
        this.builder = builder;
    }
    
    /**
     * Get the singleton instance of this client
     * @return the client instance
     */
    public static Client getInstance() {
        if(instance == null) {
            synchronized(Client.class) {
                if(instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

}
