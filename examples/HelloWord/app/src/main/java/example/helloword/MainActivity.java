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
        StaticHostProvider.addHost(new Host("127.0.0.1", 50382));

        new HelloWorldThread().start();
    }

    private static class HelloWorldThread extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    long start = System.currentTimeMillis();
                    String res = helloWorld("Josh");
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
    public static String helloWorld(String name) {
        return "Hello World, " + name;
    }

}
