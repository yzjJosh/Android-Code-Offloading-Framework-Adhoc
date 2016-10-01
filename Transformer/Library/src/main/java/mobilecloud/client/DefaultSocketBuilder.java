package mobilecloud.client;

import java.net.Socket;

/**
 * Build a java.net.Socket
 *
 */
public class DefaultSocketBuilder implements SocketBuilder {

    @Override
    public Socket build(String ip, int port) throws Exception {
        return new Socket(ip, port);
    }

}
