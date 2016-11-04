package mobilecloud.api.response;

import java.util.List;

import mobilecloud.engine.host.Host;

public class GetAvailableServerResponse extends Response {
    private static final long serialVersionUID = 1L;
    
    private List<Host> serverList;
    
    public GetAvailableServerResponse setServerList(List<Host> serverList) {
        this.serverList = serverList;
        return this;
    }
    
    public List<Host> getServerList() {
        return this.serverList;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetAvailableServerResponse{ \n");
        sb.append("    serverList: " + serverList + "\n");
        sb.append("}\n");
        return sb.toString();
    }

}
