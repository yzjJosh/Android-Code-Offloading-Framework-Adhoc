package mobilecloud.client;

import java.net.Socket;

/**
 * A builder which can builds a socket
 */
public interface SocketBuilder {
    
    /**
     * Build a socket which connects to a certain ip address and port number
     * @param ip the ip address
     * @param port the port number
     * @return the built socket
     * @throws Exception if cannot connect
     */
    public Socket build(String ip, int port) throws Exception;
    
}
