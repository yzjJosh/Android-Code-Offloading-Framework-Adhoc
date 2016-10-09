package mobilecloud.api;

/**
 * A request to upload an apk file
 */
public class UploadApplicationExecutableRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String applicationId;
    private byte[] executable;

    public String getApplicationId() {
        return applicationId;
    }

    public UploadApplicationExecutableRequest setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public byte[] getExecutable() {
        return executable;
    }

    public UploadApplicationExecutableRequest setExecutable(byte[] executable) {
        this.executable = executable;
        return this;
    }
}
