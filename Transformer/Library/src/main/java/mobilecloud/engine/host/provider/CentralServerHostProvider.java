package mobilecloud.engine.host.provider;

import java.util.List;

import mobilecloud.engine.host.Host;

public class CentralServerHostProvider implements HostProvider {
    
    private static Host staticCentralServer;
    
    private final Host centralServer;
    
    public CentralServerHostProvider() {
        this(staticCentralServer);
    }
    
    public CentralServerHostProvider(Host centralServer) {
        if(centralServer == null) {
            throw new NullPointerException();
        }
        this.centralServer = centralServer;
    }

    @Override
    public List<Host> provide() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static void init(Host centralServer) {
        staticCentralServer = centralServer;
    }

}
