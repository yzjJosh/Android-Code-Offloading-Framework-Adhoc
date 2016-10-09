package mobilecloud.engine;

import java.io.IOException;

import android.content.Context;
import mobilecloud.utils.ByteProvider;
import mobilecloud.utils.FileUtils;

/**
 * A provider that can provide apk bytes
 *
 */
public class APKByteProvider implements ByteProvider{
    
    private Context context;
    
    public APKByteProvider(Context context) {
        this.context = context;
    }

    /**
     * provide apk data
     */
    @Override
    public byte[] provide() {
        try {
            return FileUtils.readBytes(context.getPackageCodePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
