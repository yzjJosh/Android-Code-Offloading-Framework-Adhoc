package mobilecloud.api;

/**
 * Rep OK response for MonitorHostRequest
 */
public class MonitorHostResponse extends Response{
    private static final long serialVersionUID = 1L;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MonitorHostResponse{\n");
        sb.append("    success: " + isSuccess() + "\n");
        if(!isSuccess()) {
            sb.append("    throwable: " + getThrowable() + "\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
    
}
