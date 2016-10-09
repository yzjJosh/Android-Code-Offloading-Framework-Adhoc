package example.helloword;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import mobilecloud.engine.Engine;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.provider.StaticHostProvider;
import mobilecloud.utils.NetUtils;


public class MainActivity extends Activity {

    private Handler handler = new Handler();
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        Engine.localInit(this);

        new Thread() {
            @Override
            public void run() {
                StaticHostProvider.addHost(new Host(NetUtils.getLocalIpAddress(), 50382));
            }
        }.start();

        new RemoteTask().execute(1, 2);
    }

    private class RemoteTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            int res = new RemoteObject().add(params[0], params[1]);
            Log.e("HelloWorldResult", RemoteObject.helloWorld());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new RemoteTask().execute(1, 2);
                }
            }, 5000);
            return res;
        }

        @Override
        protected void onPostExecute(Integer result) {
            textView.setText(String.valueOf(result));
        }
    }

}
