package mobilecloud.api.response;

import mobilecloud.metric.Metric;

/**
 * Rep OK response for MonitorHostRequest
 */
public class MonitorHostResponse extends Response{
    private static final long serialVersionUID = 1L;
    
    private Metric metric;
    
    public MonitorHostResponse setMetric(Metric metric) {
        this.metric = metric;
        return this;
    }
    
    public Metric getMetric() {
        return metric;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MonitorHostResponse{\n");
        sb.append("    success: " + isSuccess() + "\n");
        if(isSuccess()) {
            sb.append("    metric: " + getMetric() + "\n");
        } else {
            sb.append("    throwable: " + getThrowable() + "\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
    
}
