package example.helloword;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobilecloud.lib.Remote;


public class MainActivity extends Activity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
    }

    private class Task extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            long start = System.currentTimeMillis();
            String res = helloWorld(params[0]);
            long end = System.currentTimeMillis();
            return "helloWord() result is " + res + ", spending time is " + (end - start);
        }

        @Override
        protected void onPostExecute(String res) {
            TextView txt = (TextView) findViewById(R.id.textView);
            txt.setText(res);
        }
    }

    @Override
    public void onClick(View v) {
        new Task().execute("Josh");
    }


    @Remote
    public static String helloWorld(String name) {
        return "Hello World, " + name;
    }

}
