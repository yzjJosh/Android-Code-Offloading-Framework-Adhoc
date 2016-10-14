package mobilecloud.server.receiver;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import mobilecloud.api.Request;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.utils.FileUtils;
import mobilecloud.utils.ObjectInputStreamWrapper;
import mobilecloud.utils.ObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestReceiver implements Receiver {

    private ExecutableLoader loader;
    private Set<String> uploadingApps;
    
    public UploadApplicationExecutableRequestReceiver(ExecutableLoader loader) {
        this.loader = loader;
        this.uploadingApps = new HashSet<>();
    }
    
    @Override
    public Request receive(ObjectInputStreamWrapper is, ObjectOutputStreamWrapper os) throws Exception {
        UploadApplicationExecutableRequest req = (UploadApplicationExecutableRequest) is.get().readObject();
        
        // Make sure that for every app there is only one thread trying to upload executable
        synchronized(uploadingApps) {
            while(uploadingApps.contains(req.getApplicationId())) {
                try {
                    uploadingApps.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            uploadingApps.add(req.getApplicationId());
        }
        
        try {
            
            if (FileUtils.hasFiles(loader.getExecutableDirectory(req.getApplicationId()))
                    || FileUtils.fileExists(loader.getTmpExecutablePackLocation(req.getApplicationId()))) {
                // If executable already exists, tell the client and throw an
                // exception
                os.get().writeBoolean(false);
                os.get().flush();
                throw new DuplicateExecutableException();
            } else {
                // Otherwise, it is OK to receive data
                os.get().writeBoolean(true);
                os.get().flush();
            }
            
            // Write data to the temp location
            FileUtils.createDirIfDoesNotExist(loader.getTmpDirectory(req.getApplicationId()));
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(loader.getTmpExecutablePackLocation(req.getApplicationId()));

                int len = 0;
                while ((len = is.get().readInt()) != -1) {
                    fileOut.write((byte[]) is.get().readObject(), 0, len);
                }
            } finally {
                if (fileOut != null) {
                    fileOut.close();
                }
            }
            
            // Set path to the location of executable
            req.setExecutablePath(loader.getTmpExecutablePackLocation(req.getApplicationId()));
            return req;
        } finally {
            synchronized(uploadingApps) {
                uploadingApps.remove(req.getApplicationId());
                uploadingApps.notifyAll();
            }
        }
    }

}
