package mobilecloud.engine;

/**
 * A representation of cloud host
 */
public class Host {
    public final String ip;
    public final int port;

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
