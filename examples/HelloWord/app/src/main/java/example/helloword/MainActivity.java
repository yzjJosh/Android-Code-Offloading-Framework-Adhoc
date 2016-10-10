package example.helloword;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;

import mobilecloud.engine.Engine;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.provider.StaticHostProvider;
import mobilecloud.lib.Remote;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize engine
        Engine.localInit(this);

        // Set server ip and port
        StaticHostProvider.addHost(new Host("192.168.0.11", 50382));

        new HelloWorldThread().start();
    }

    private static class HelloWorldThread extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    long start = System.currentTimeMillis();
                    String res = helloWorld();
                    long end = System.currentTimeMillis();
                    Log.e(TAG, "helloWord() result is " + res + ", spending time is " + (end - start));
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Remote
    public static String helloWorld() {
        try {
            Method method = MainActivity.class.getMethod("helloWorld");
            if(Engine.getInstance().shouldMigrate(method, null)) {
                Log.e(TAG, "Running helloWorld remotely ...");
                return (String) Engine.getInstance().invokeRemotely(method, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Running helloWorld locally ...");
        return "Hello World";
    }

}
