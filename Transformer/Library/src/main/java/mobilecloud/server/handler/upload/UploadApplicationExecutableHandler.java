package mobilecloud.server.handler.upload;

import java.io.File;
import java.io.FileOutputStream;

import org.zeroturnaround.zip.ZipUtil;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.api.UploadApplicationExecutableResponse;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.Server;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.FileUtils;

/**
 * A handler to accept apk files and load them into class loader
 */
public class UploadApplicationExecutableHandler implements Handler {
    
    private final Server server;
    private final ExecutableLoader executableLoader;
    
    public UploadApplicationExecutableHandler(Server server, ExecutableLoader apkLoader) {
        this.server = server;
        this.executableLoader = apkLoader;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest upReq = (UploadApplicationExecutableRequest) request;
        UploadApplicationExecutableResponse resp = new UploadApplicationExecutableResponse();

        // Write executable file to tmp folder
        FileOutputStream fileOuputStream = null;
        try {
            FileUtils.createDirIfDoesNotExist(executableLoader.getTmpDirectory(upReq.getApplicationId()));
            fileOuputStream = new FileOutputStream(
                    executableLoader.getTmpExecutablePackLocation(upReq.getApplicationId()));
            fileOuputStream.write(upReq.getExecutable());
        } finally {
            if (fileOuputStream != null) {
                fileOuputStream.close();
            }
        }

        // unzip the executables
        ZipUtil.unpack(new File(executableLoader.getTmpExecutablePackLocation(upReq.getApplicationId())),
                new File(executableLoader.getExecutableDirectory(upReq.getApplicationId())));

        // Remove tmp file
        FileUtils.deleteFolder(executableLoader.getTmpDirectory(upReq.getApplicationId()));
        
        try {
            ClassLoader cl = executableLoader.loadExecutable(upReq.getApplicationId());
            server.registerClassLoader(upReq.getApplicationId(), cl);
            return resp.setSuccess(true);
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }
    }

}
