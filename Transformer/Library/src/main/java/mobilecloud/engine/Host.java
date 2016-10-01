package mobilecloud.engine;

import lombok.NonNull;

/**
 * A representation of cloud host
 */
public class Host {
    public final String ip;
    public final int port;
    
    private String str;

    public Host(@NonNull String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != Host.class) {
            return false;
        } else {
            Host that = (Host) o;
            return this.ip.equals(that.ip) && this.port == that.port;
        }
    }
    
    @Override
    public String toString() {
        if(str == null) {
            str = ip + ":" + port;
        }
        return str;
    }
    
}
