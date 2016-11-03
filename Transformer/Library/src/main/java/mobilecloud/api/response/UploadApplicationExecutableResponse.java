package mobilecloud.api.response;

/**
 * Response to an uploading
 */
public class UploadApplicationExecutableResponse extends Response{
    private static final long serialVersionUID = 1L;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UploadApplicationExecutableResponse{\n");
        sb.append("    success: " + isSuccess() + "\n");
        if(!isSuccess()) {
            sb.append("    throwable: " + getThrowable() + "\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
