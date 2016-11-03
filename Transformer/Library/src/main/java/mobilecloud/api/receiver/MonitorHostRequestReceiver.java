package mobilecloud.api.receiver;

import mobilecloud.api.request.MonitorHostRequest;
import mobilecloud.api.request.Request;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class MonitorHostRequestReceiver implements Receiver {

    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        return (MonitorHostRequest) is.get().readObject();
    }


}
