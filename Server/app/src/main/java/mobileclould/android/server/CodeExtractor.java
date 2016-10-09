package mobileclould.android.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mobilecloud.utils.FileUtils;

public class CodeExtractor {

    private static final int MAX_BUFFER = 1 << 30;

    public List<String> extract(String apkPath, String outputDir) throws Exception {
        List<String> res = new LinkedList<>();
        FileUtils.createDirIfDoesNotExist(outputDir);
        ZipFile apkFile = new ZipFile(apkPath);
        for(Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) apkFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = entries.nextElement();
            if(entry.getName().endsWith(".dex")) {
                File dex = new File(outputDir + "/" + entry.getName());
                if(dex.exists()) {
            //        continue;
                }
                BufferedInputStream is = new BufferedInputStream(apkFile.getInputStream(entry));
                FileOutputStream fos = new FileOutputStream(dex);
                int buffer = Math.min((int) entry.getSize(), MAX_BUFFER);
                BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);
                byte data[] = new byte[buffer];
                int count = 0;
                while ((count = is.read(data, 0, buffer)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
                res.add(dex.getPath());
            }
        }
        return res;
    }

}
