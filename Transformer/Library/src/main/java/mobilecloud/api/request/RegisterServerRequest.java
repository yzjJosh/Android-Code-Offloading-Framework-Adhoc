package mobilecloud.api.request;

public class RegisterServerRequest extends Request {
    private static final long serialVersionUID = 1L;

    private int serverPort;
    
    public RegisterServerRequest setServerPort(int port) {
        this.serverPort = port;
        return this;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegisterServerRequest{ \n");
        sb.append("    serverPort: " + serverPort + "\n");
        sb.append("}\n");
        return sb.toString();
    }
    
}
