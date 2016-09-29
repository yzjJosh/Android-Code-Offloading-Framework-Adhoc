package mobilecloud.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import lombok.NonNull;
import mobilecloud.utils.DataString;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

/**
 * A client which interacts with server
 *
 */
public class Client {
    
    private static final String CHAR_SET = "UTF-8";
    
    private static Client instance;
    
    private Client() {}
    
    /**
     * Send a request to the server and waits for response
     * @param request the request
     * @return the response
     * @throws Exception if any problem occurs during the request
     */
    public Response request(@NonNull Request request) throws Exception {
        DataString req = new DataString(request);
        Socket socket = new Socket(request.getIp(), request.getPort());
        try {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.write(req.toString().getBytes(Charset.forName(CHAR_SET)));
            os.flush();
            DataString resp = new DataString(IOUtils.toString(is, CHAR_SET));
            return (Response) resp.deserialize();
        } finally {
            socket.close();
        }
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
