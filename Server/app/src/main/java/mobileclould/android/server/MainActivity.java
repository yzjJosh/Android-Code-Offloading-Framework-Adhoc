package mobileclould.android.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import mobilecloud.utils.FileUtils;
import mobileclould.android.server.service.ServerService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //    runService(Config.PORT_NUMBER);

      //  String apk = "/data/local/tmp/example.helloword";
        String apk = "/data/local/tmp/com.josh.profrate";
       // DexClassLoader cl = new DexClassLoader(root + "/app-debug.apk", getCacheDir().getPath(), null, ClassLoader.getSystemClassLoader());
      //  CodeHandler handler = new CodeHandler(this);
      //  handler.LoadAPK(root + "/app-debug.apk");
     //   Class<?> clazz = handler.getClass("example/helloword/RemoteObject");
        Log.e("test", new File(apk).exists() + "");
        try {
            Log.e("test", FileUtils.readBytes(apk).length + "");
            List<String> dexes = new CodeExtractor().extract(apk, getCacheDir().getPath());
            Log.d("test", dexes.toString());
            Class<?> clazz = null;
            for(String dex: dexes) {
              /*  PathClassLoader loader = new PathClassLoader(dex, getClassLoader());
                try {
               //     clazz = loader.loadClass("com.josh.profrate.elements.Credential");
                    clazz = loader.loadClass("example.helloword.RemoteObject");
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                DexFile d = new DexFile(dex);
          //      clazz = d.loadClass("example.helloword.RemoteObject", getClassLoader());
                clazz = d.loadClass("com.josh.profrate.elements.Credential", getClassLoader());
                if(clazz != null) {
                    break;
                }
            }
            Log.e("Test", clazz.getName());
        //    PathClassLoader cl = new PathClassLoader(root + "/app-debug.apk", getClassLoader());
        //    Class<?> clazz = cl.loadClass("com.josh.profrate.elements.Credential");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Start the service on given port number
    private void runService(int port) {
        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra(ServerService.PORT_NUMBER_KEY, port);
        startService(intent);
    }
}
