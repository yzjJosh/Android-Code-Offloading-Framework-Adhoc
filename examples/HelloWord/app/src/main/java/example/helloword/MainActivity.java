package example.helloword;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;

import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.provider.StaticHostProvider;
import mobilecloud.lib.Remote;
import mobilecloud.lib.listener.RemoteExecutionListener;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private static class Listener implements RemoteExecutionListener {

        @Override
        public boolean onRemoteExecutionStart(Method method, Object o, Object[] objects) {
            Log.e("test", "start invoking remotely ...");
            return true;
        }

        @Override
        public void onRemoteExecutionComplete(Method method, Object o, Object[] objects, Object o1, boolean b, Throwable throwable) {
            Log.e("test", "completes, status is " + (b? "success": "failed"));
        }
    }

    @Remote(listener = Listener.class)
    public static String helloWorld(String name) {
        return "Hello World, " + name;
    }

}
