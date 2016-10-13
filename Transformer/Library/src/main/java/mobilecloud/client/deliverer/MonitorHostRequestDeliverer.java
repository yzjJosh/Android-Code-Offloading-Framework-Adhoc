package mobilecloud.client.deliverer;

import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class MonitorHostRequestDeliverer implements Deliverer {

    @Override
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof MonitorHostRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        
        // Write request
        os.get().writeObject(request);
        os.get().flush();
    }

}
