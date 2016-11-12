package mobilecloud.engine.host;

/**
 * Local host represents current local host
 *
 */
public class LocalHost extends Host {
    
    private static final long serialVersionUID = 1L;

    public LocalHost() {
        super("localHost", 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != LocalHost.class) {
            return false;
        } else {
            LocalHost that = (LocalHost) o;
            Host me = new Host(ip, port);
            Host he = new Host(that.ip, that.port);
            return me.equals(he);
        }
    }

}
