package mobilecloud.api.receiver;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.utils.AdvancedObjectInputStream;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class RemoteInvocationRequestReceiver implements Receiver {
    
    private final ExecutableLoader exeLoader;
    
    public RemoteInvocationRequestReceiver(ExecutableLoader exeLoader) {
        this.exeLoader = exeLoader;
    }

    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        String appId = (String) is.get().readObject();
        ClassLoader cl = null;
        try {
            cl = exeLoader.loadExecutable(appId);
            os.get().writeBoolean(true);
            os.get().flush();
        } catch (NoApplicationExecutableException e) {
            os.get().writeBoolean(false);
            os.get().flush();
            throw e;
        }
        AdvancedObjectInputStream stream = (AdvancedObjectInputStream) is.get();
        stream.setClassLoader(cl);
        RemoteInvocationRequest res = (RemoteInvocationRequest) stream.readObject();
        res.setApplicationId(appId);
        return res;
    }


}
