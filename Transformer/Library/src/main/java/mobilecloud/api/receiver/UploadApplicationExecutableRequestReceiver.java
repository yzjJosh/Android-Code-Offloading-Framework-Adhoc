package mobilecloud.api.receiver;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import mobilecloud.api.request.Request;
import mobilecloud.api.request.UploadApplicationExecutableRequest;
import mobilecloud.metric.MetricGenerator;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.utils.FileUtils;
import mobilecloud.utils.AdvancedObjectInputStreamWrapper;
import mobilecloud.utils.AdvancedObjectOutputStreamWrapper;

public class UploadApplicationExecutableRequestReceiver implements Receiver<Request> {

    private final ExecutableLoader loader;
    private final Set<String> uploadingApps;
    private final MetricGenerator metricGenerator;
    
    public UploadApplicationExecutableRequestReceiver(ExecutableLoader loader) {
        this(loader, null);
    }
    
    public UploadApplicationExecutableRequestReceiver(ExecutableLoader loader, MetricGenerator metricGenerator) {
        this.loader = loader;
        this.uploadingApps = new HashSet<>();
        this.metricGenerator = metricGenerator;
    }
    
    @Override
    public Request receive(AdvancedObjectInputStreamWrapper is, AdvancedObjectOutputStreamWrapper os) throws Exception {
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
                os.get().resetStat();
                os.get().writeBoolean(false);
                os.get().flush();
                if(metricGenerator != null) {
                    metricGenerator.reportWrite(os.get().getBytesWritten());
                }
                throw new DuplicateExecutableException();
            } else {
                // Otherwise, it is OK to receive data
                os.get().resetStat();
                os.get().writeBoolean(true);
                os.get().flush();
                if(metricGenerator != null) {
                    metricGenerator.reportWrite(os.get().getBytesWritten());
                }
            }
            
            // Write data to the temp location
            FileUtils.createDirIfDoesNotExist(loader.getTmpDirectory(req.getApplicationId()));
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(loader.getTmpExecutablePackLocation(req.getApplicationId()));

                int len = 0;
                is.get().resetStat();
                while ((len = is.get().readInt()) != -1) {
                    fileOut.write((byte[]) is.get().readObject(), 0, len);
                    if(metricGenerator != null) {
                        metricGenerator.reportRead(is.get().getBytesRead());
                    }
                    is.get().resetStat();
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
