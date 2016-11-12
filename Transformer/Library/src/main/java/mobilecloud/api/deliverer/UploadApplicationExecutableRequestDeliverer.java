package mobilecloud.api.deliverer;

import java.io.FileInputStream;

import mobilecloud.api.request.Request;
import mobilecloud.api.request.UploadApplicationExecutableRequest;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestDeliverer extends SimpleDeliverer<Request> {
    
    private final static int bufferSize = 1<<16;
    private MetricGenerator metricGenerator;
    
    public UploadApplicationExecutableRequestDeliverer() {
        this(null);
    }
    
    public UploadApplicationExecutableRequestDeliverer(MetricGenerator metricGenerator) {
        this.metricGenerator = metricGenerator;
    }

    @Override
    public void deliver(Request request, AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest req = (UploadApplicationExecutableRequest) request;
        
        String exePath = req.getExecutablePath();
        
        // No need to send local executable path
        req.setExecutablePath(null);
        
        os.resetStat();
        is.resetStat();
        
        try {
            
            // Write request firstly
            os.get().writeObject(request);
            os.get().flush();
            
            // Read response from server
            boolean OK = is.get().readBoolean();
            if(OK) {
                // Server does not have app executable, send it to server
                FileInputStream input = null;
                try {
                    input = new FileInputStream(exePath);
                    byte[] buffer = new byte[bufferSize];
                    while(true) {
                        int len = input.read(buffer);
                        
                        // Write length firstly
                        os.get().writeInt(len);
                        os.get().flush();
                        
                        if(len != -1) {
                            // If has content, write contents
                            os.get().writeObject(buffer);
                            os.get().flush();
                            os.get().reset();
                        } else {
                            break;
                        }
                    }
                } finally {
                    if(input != null) {
                        input.close();
                    }
                }
            } else {
                // Server already has app executable, abort this uploading
                throw new DuplicateExecutableException();
            }
        } finally {
            if(metricGenerator != null) {
                metricGenerator.reportRead(is.get().getBytesRead());
                metricGenerator.reportWrite(os.get().getBytesWritten());
            }
        }
    }

}
