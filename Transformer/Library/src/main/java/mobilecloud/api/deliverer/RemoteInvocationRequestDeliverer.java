package mobilecloud.api.deliverer;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RemoteInvocationRequestDeliverer implements Deliverer<Request> {
    
    private final MetricGenerator metricGenerator;
    
    public RemoteInvocationRequestDeliverer() {
        this(null);
    }
    
    public RemoteInvocationRequestDeliverer(MetricGenerator generator) {
        this.metricGenerator = generator;
    }

    @Override
    public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof RemoteInvocationRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        
        RemoteInvocationRequest req = (RemoteInvocationRequest) request;
        
        //write application id
        os.get().resetStat();
        os.get().writeObject(req.getApplicationId());
        os.get().flush();
        if(metricGenerator != null) {
            metricGenerator.reportWrite(os.get().getBytesWritten());
        }
        
        //Wait for OK
        is.get().resetStat();
        boolean OK = is.get().readBoolean();
        if(metricGenerator != null) {
            metricGenerator.reportRead(is.get().getBytesRead());
        }
        if(!OK) {
            //If does not have executable, throw exception
            throw new NoApplicationExecutableException();
        }
        
        // No need to resend app id
        req.setApplicationId(null);
        
        // Deliver request
        os.get().resetStat();
        os.get().writeObject(req);
        os.get().flush();
        if(metricGenerator != null) {
            metricGenerator.reportWrite(os.get().getBytesWritten());
        }
    }

}
