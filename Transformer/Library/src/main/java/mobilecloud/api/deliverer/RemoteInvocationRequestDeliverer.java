package mobilecloud.api.deliverer;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class RemoteInvocationRequestDeliverer implements Deliverer {

    @Override
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof RemoteInvocationRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        
        RemoteInvocationRequest req = (RemoteInvocationRequest) request;
        
        //write application id
        os.get().writeObject(req.getApplicationId());
        os.get().flush();
        
        //Wait for OK
        boolean OK = is.get().readBoolean();
        if(!OK) {
            //If does not have executable, throw exception
            throw new NoApplicationExecutableException();
        }
        
        // No need to resend app id
        req.setApplicationId(null);
        
        // Deliver request
        os.get().writeObject(req);
        os.get().flush();
    }

}
