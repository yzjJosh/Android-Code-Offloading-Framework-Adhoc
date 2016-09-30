package mobilecloud.upload;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mobilecloud.utils.Request;

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
