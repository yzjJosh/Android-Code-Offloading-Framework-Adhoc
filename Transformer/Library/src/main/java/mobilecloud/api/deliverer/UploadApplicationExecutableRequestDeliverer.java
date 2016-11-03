package mobilecloud.api.deliverer;

import java.io.FileInputStream;

import mobilecloud.api.request.Request;
import mobilecloud.api.request.UploadApplicationExecutableRequest;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestDeliverer implements Deliverer {
    
    private final static int bufferSize = 1<<16;

    @Override
    public void deliver(Request request, ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os)
            throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest req = (UploadApplicationExecutableRequest) request;
        
        String exePath = req.getExecutablePath();
        
        // No need to send local executable path
        req.setExecutablePath(null);
        
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
    }

}
