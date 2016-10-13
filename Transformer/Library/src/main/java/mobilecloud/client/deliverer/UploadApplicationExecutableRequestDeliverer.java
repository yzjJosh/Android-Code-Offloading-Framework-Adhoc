package mobilecloud.client.deliverer;

import mobilecloud.api.Request;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestDeliverer implements Deliverer {

    @Override
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
       // UploadApplicationExecutableRequest req = (UploadApplicationExecutableRequest) request;
        
        // Write request
        os.get().writeObject(request);
        os.get().flush();
    }

}
