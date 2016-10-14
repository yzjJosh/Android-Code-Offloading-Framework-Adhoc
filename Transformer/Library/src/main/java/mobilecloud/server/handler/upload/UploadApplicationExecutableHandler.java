package mobilecloud.server.handler.upload;

import java.io.File;

import org.zeroturnaround.zip.ZipUtil;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.api.UploadApplicationExecutableRequest;
import mobilecloud.api.UploadApplicationExecutableResponse;
import mobilecloud.server.DuplicateExecutableException;
import mobilecloud.server.ExecutableLoader;
import mobilecloud.server.NoApplicationExecutableException;
import mobilecloud.server.handler.Handler;
import mobilecloud.utils.FileUtils;

/**
 * A handler to accept apk files and load them into class loader
 */
public class UploadApplicationExecutableHandler implements Handler {
    
    private final ExecutableLoader executableLoader;
    
    public UploadApplicationExecutableHandler(ExecutableLoader executableLoader) {
        this.executableLoader = executableLoader;
    }

    @Override
    public Response handle(Request request) throws Exception {
        if (!(request instanceof UploadApplicationExecutableRequest)) {
            throw new IllegalArgumentException(request.toString());
        }
        UploadApplicationExecutableRequest upReq = (UploadApplicationExecutableRequest) request;
        UploadApplicationExecutableResponse resp = new UploadApplicationExecutableResponse();

        // If the executable already exists, ignore this uploading.
        try {
            executableLoader.loadExecutable(upReq.getApplicationId());
            return resp.setSuccess(false).setThrowable(new DuplicateExecutableException("Executable already exists!"));
        } catch (NoApplicationExecutableException e) {}


        // unzip the executables
        ZipUtil.unpack(new File(upReq.getExecutablePath()),
                new File(executableLoader.getExecutableDirectory(upReq.getApplicationId())));

        // Remove tmp file
        FileUtils.deleteFolder(upReq.getExecutablePath());

        try {
            // Load the executable to memory
            executableLoader.loadExecutable(upReq.getApplicationId());
            return resp.setSuccess(true);
        } catch (Exception e) {
            return resp.setSuccess(false).setThrowable(e);
        }

    }

}
