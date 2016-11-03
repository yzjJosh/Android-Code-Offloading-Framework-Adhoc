package mobilecloud.api.request;

/**
 * A request to upload an apk file
 */
public class UploadApplicationExecutableRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String applicationId;
    private String executablePath;

    public String getApplicationId() {
        return applicationId;
    }

    public UploadApplicationExecutableRequest setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public UploadApplicationExecutableRequest setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UploadApplicationExecutableRequest{\n");
        sb.append("    applicationId: " + getApplicationId() + "\n");
        sb.append("}\n");
        return sb.toString();
    }

}
