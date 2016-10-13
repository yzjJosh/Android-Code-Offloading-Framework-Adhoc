package mobilecloud.server.receiver;

import mobilecloud.api.Request;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestReceiver implements Receiver {

    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        return (UploadApplicationExecutableRequest) is.get().readObject();
    }

}
