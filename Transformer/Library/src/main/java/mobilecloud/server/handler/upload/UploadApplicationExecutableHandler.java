package mobilecloud.server.handler.upload;

import java.io.FileOutputStream;

import lombok.NonNull;
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
    
    private Server server;
    
    public UploadApplicationExecutableHandler(@NonNull Server server) {
        this.server = server;
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
            FileUtils.createDirIfDoesNotExist(APKLoader.getAppDirectory(upReq.getApplicationId()));
            fileOuputStream = new FileOutputStream(APKLoader.getExecutableLocation(upReq.getApplicationId()));
            fileOuputStream.write(upReq.getExecutable());
        } finally {
            if (fileOuputStream != null) {
                fileOuputStream.close();
            }
        }
        try {
            ClassLoader cl = new APKLoader().loadAPK(upReq.getApplicationId());
            server.registerClassLoader(upReq.getApplicationId(), cl);
            return resp.setSuccess(true);
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
