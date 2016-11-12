package mobilecloud.api.receiver;

import mobilecloud.api.request.RemoteInvocationRequest;
import mobilecloud.api.request.Request;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class RemoteInvocationRequestReceiver implements Receiver<Request> {
    
    private final ExecutableLoader exeLoader;
    private final MetricGenerator metricGenerator;
    
    public RemoteInvocationRequestReceiver(ExecutableLoader exeLoader) {
        this(exeLoader, null);
    }
    
    public RemoteInvocationRequestReceiver(ExecutableLoader exeLoader, MetricGenerator metricGenerator) {
        this.exeLoader = exeLoader;
        this.metricGenerator = metricGenerator;
    }

    @Override
    public Request receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
        is.resetStat();
        os.resetStat();
        
        try {
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
            is.get().setClassLoader(cl);
            RemoteInvocationRequest res = (RemoteInvocationRequest) is.get().readObject();

            res.setApplicationId(appId);
            return res;
        } finally {
            if (metricGenerator != null) {
                metricGenerator.reportRead(is.get().getBytesRead());
                metricGenerator.reportWrite(os.get().getBytesWritten());
            }
        }
    }


}
