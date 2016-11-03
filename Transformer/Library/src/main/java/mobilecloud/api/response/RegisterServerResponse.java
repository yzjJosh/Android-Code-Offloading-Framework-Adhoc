package mobilecloud.api.response;

public class RegisterServerResponse extends Response {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegisterServerResponse{\n");
        sb.append("    success: " + isSuccess() + "\n");
        if(!isSuccess()) {
            sb.append("    throwable: " + getThrowable() + "\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
