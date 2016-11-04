package mobilecloud.engine.host.provider;

import java.util.LinkedList;
import java.util.List;

import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.api.response.GetAvailableServerResponse;
import mobilecloud.api.response.Response;
import mobilecloud.client.Client;
import mobilecloud.engine.Config;
import mobilecloud.engine.host.Host;

public class CentralServerHostProvider implements HostProvider {
    
    private final Host centralServer;
    private final Client client;
    
    public CentralServerHostProvider() {
        this(new Host(Config.CENTRAL_SERVER_IP_ADDR, Config.CENTRAL_SERVER_PORT_NUMBER), Client.getInstance());
    }
    
    public CentralServerHostProvider(Host centralServer, Client client) {
        if(centralServer == null || client == null) {
            throw new NullPointerException();
        }
        this.centralServer = centralServer;
        this.client = client;
    }

    @Override
    public List<Host> provide() {
        Request req = new GetAvailableServerRequest().setIp(centralServer.ip).setPort(centralServer.port);
        Response resp = null;
        try {
            resp = client.request(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(resp == null || !resp.isSuccess()) {
            return new LinkedList<>();
        } else {
            return ((GetAvailableServerResponse) resp).getServerList();
        }
    }
}
