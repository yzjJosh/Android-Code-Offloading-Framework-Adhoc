package mobilecloud.api;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A request to upload an apk file
 */
@Setter
@Getter
@Accessors(chain = true)
public class UploadApplicationExecutableRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String applicationId;
    private byte[] executable;
}
