package mobilecloud.api.receiver;

import mobilecloud.api.request.GetAvailableServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class GetAvailableServerRequestReceiver implements Receiver{

    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        return (GetAvailableServerRequest) is.get().readObject();
    }

}
