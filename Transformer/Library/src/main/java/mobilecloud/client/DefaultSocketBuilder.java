package mobilecloud.client;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Build a java.net.Socket
 *
 */
public class DefaultSocketBuilder implements SocketBuilder {

    @Override
    public Socket build(String ip, int port, int timeout) throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout);
        return socket;
    }

}
