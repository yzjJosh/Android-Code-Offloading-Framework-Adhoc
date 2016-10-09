package mobilecloud.engine;

import java.io.File;
import java.io.IOException;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import android.content.Context;
import mobilecloud.utils.ByteProvider;
import mobilecloud.utils.FileUtils;

/**
 * A provider that can provide executable bytes
 *
 */
public class ExecutableByteProvider implements ByteProvider{
    
    private Context context;
    
    public ExecutableByteProvider(Context context) {
        this.context = context;
    }
    
    public String getExecutableFilePath() {
        return context.getCacheDir() + "/" + Config.EXECUTABLE_FOLDER + "/" + Config.EXECUTABLE_NAME;
    }
    
    public String getTempOutputDir() {
        return context.getCacheDir() + "/" + Config.EXECUTABLE_FOLDER + "/" + Config.TEMP_OUTPUT_FOLDER;
    }

    /**
     * provide executable data
     */
    @Override
    public byte[] provide() {
        try {
            if(!new File(getExecutableFilePath()).exists()) {
                FileUtils.createDirIfDoesNotExist(getTempOutputDir());
                recursiveUnZip(context.getPackageCodePath());
                ZipUtil.pack(new File(getTempOutputDir()), new File(getExecutableFilePath()));
                FileUtils.deleteFolder(getTempOutputDir());
            }
            return FileUtils.readBytes(getExecutableFilePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void recursiveUnZip(String path) {
        final File source = new File(path);
        final File out = new File(getTempOutputDir());
        ZipUtil.unpack(source, out, new NameMapper() {
            @Override
            public String map(String name) {
                if(name.endsWith(".dex")) {
                    return name;
                } else {
                    if(name.endsWith(".zip")) {
                        File zip = new File(getTempOutputDir() + "/" + name);
                        ZipUtil.unpackEntry(source, name, zip);
                        recursiveUnZip(zip.getPath());
                        zip.delete();
                    }
                    return null;
                }
            }
        });
    }

}
