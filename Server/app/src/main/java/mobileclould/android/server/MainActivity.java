package mobileclould.android.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import mobileclould.android.server.service.ServerService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runService(Config.PORT_NUMBER);
    }

    //Start the service on given port number
    private void runService(int port) {
        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra(ServerService.PORT_NUMBER_KEY, port);
        startService(intent);
    }
}
