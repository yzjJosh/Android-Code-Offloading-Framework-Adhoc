package mobilecloud.api.receiver;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.api.request.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class RegisterServerRequestReceiver implements Receiver{

    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        return (RegisterServerRequest) is.get().readObject();
    }

}
