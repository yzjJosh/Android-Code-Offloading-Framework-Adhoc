package mobilecloud.upload;

import java.io.FileOutputStream;

import dalvik.system.DexClassLoader;
import mobilecloud.server.Handler;
import mobilecloud.server.Server;
import mobilecloud.utils.FileUtils;
import mobilecloud.utils.Request;
import mobilecloud.utils.Response;

/**
 * A handler to accept apk files and load them into class loader
 */
public class UploadApplicationExecutableHandler implements Handler {

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest upReq = (UploadApplicationExecutableRequest) request;
        UploadApplicationExecutableResponse resp = new UploadApplicationExecutableResponse();
        FileOutputStream fileOuputStream = null;
        try {
            FileUtils.createDirIfDoesNotExist(FileUtils.getAppDirectory(upReq.getApplicationId()));
            fileOuputStream = new FileOutputStream(FileUtils.getExecutableLocation(upReq.getApplicationId()));
            fileOuputStream.write(upReq.getExecutable());
        } finally {
            if (fileOuputStream != null) {
                fileOuputStream.close();
            }
        }
        try {
            FileUtils.createDirIfDoesNotExist(FileUtils.getOptimizedDexDirectory(upReq.getApplicationId()));
            DexClassLoader dcl = new DexClassLoader(FileUtils.getExecutableLocation(upReq.getApplicationId()),
                    FileUtils.getOptimizedDexDirectory(upReq.getApplicationId()), null,
                    ClassLoader.getSystemClassLoader());
            Server.getInstance().registerClassLoader(upReq.getApplicationId(), dcl);
            return resp.setSuccess(true);
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
