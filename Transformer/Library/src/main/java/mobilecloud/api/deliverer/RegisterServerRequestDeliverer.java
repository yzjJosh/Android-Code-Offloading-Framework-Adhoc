package mobilecloud.api.deliverer;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class RegisterServerRequestDeliverer implements Deliverer{

    @Override
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        if (!(request instanceof RegisterServerRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        
        // Write request
        os.get().writeObject(request);
        os.get().flush();
    }

}
