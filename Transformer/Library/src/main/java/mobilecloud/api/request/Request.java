package mobilecloud.api.request;

import java.io.Serializable;

/**
 * Abstract request class
 */
public abstract class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fromIp;
    private transient String ip;
    private transient int port;

    public String getIp() {
        return ip;
    }

    public Request setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Request setPort(int port) {
        this.port = port;
        return this;
    }
    
    public String getFromIp() {
        return fromIp;
    }
    
    public Request setFromIp(String fromIp) {
        this.fromIp = fromIp;
        return this;
    }
}
