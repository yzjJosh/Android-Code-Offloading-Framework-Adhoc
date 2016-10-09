package mobilecloud.server.handler.upload;

import java.io.FileOutputStream;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.api.UploadApplicationExecutableResponse;
import mobilecloud.server.APKLoader;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.FileUtils;

/**
 * A handler to accept apk files and load them into class loader
 */
public class UploadApplicationExecutableHandler implements Handler {
    
    private final Server server;
    private final APKLoader apkLoader;
    
    public UploadApplicationExecutableHandler(Server server, APKLoader apkLoader) {
        this.server = server;
        this.apkLoader = apkLoader;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest upReq = (UploadApplicationExecutableRequest) request;
        UploadApplicationExecutableResponse resp = new UploadApplicationExecutableResponse();
        FileOutputStream fileOuputStream = null;
        try {
            FileUtils.createDirIfDoesNotExist(apkLoader.getAppDirectory(upReq.getApplicationId()));
            fileOuputStream = new FileOutputStream(apkLoader.getExecutableLocation(upReq.getApplicationId()));
            fileOuputStream.write(upReq.getExecutable());
        } finally {
            if (fileOuputStream != null) {
                fileOuputStream.close();
            }
        }
        try {
            ClassLoader cl = apkLoader.loadAPK(upReq.getApplicationId());
            server.registerClassLoader(upReq.getApplicationId(), cl);
            return resp.setSuccess(true);
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
