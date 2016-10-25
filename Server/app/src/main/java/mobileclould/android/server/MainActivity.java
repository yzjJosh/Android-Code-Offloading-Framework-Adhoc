package mobileclould.android.server;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import mobilecloud.api.MonitorHostRequest;
import mobilecloud.api.MonitorHostResponse;
import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.ServerListener;
import mobileclould.android.server.logger.ConsoleTree;
import mobileclould.android.server.logger.LogCatTree;
import mobileclould.android.server.service.ServerService;
import timber.log.Timber;

public class MainActivity extends Activity implements ServiceConnection, ServerListener {

    private static final int CONSOLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int CONSOLE_TEXT_SIZE = 13;

    private ServerService service;

    // Initialize the Timber logger
    static {
        ConsoleTree consoleTree = new ConsoleTree.Builder().assertColor(Color.CYAN)
                .verboseColor(Color.DKGRAY).debugColor(Color.GRAY).
                infoColor(Color.BLACK).warnColor(Color.YELLOW).
                errorColor(Color.RED).minPriority(Log.INFO)
                .build();
        Timber.plant(consoleTree);
        Timber.plant(new LogCatTree());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConsole();

        runService(Config.PORT_NUMBER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(this);
    }

    // Initialize the console
    private void initConsole() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.console_scroll_view);
        scrollView.setBackgroundColor(CONSOLE_BACKGROUND_COLOR);
        TextView textView = (TextView) findViewById(R.id.console_text);
        textView.setTextSize(CONSOLE_TEXT_SIZE);
    }

    //Start the service on given port number
    private void runService(int port) {
        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra(ServerService.PORT_NUMBER_KEY, port);
        startService(intent);
        bindService(intent, this, BIND_IMPORTANT);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((ServerService.ServiceBinder) service).getService();
        this.service.registerServerListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.service.registerServerListener(null);
        this.service = null;
    }

    @Override
    public void onRequestReceivingStarts(String s) {
        Timber.d("Start receiving request %s.", s);
    }

    @Override
    public void onRequestReceived(Request request) {
        int priority = request.getClass() == MonitorHostRequest.class? Log.DEBUG: Log.INFO;
        Timber.log(priority, "Receive request %s.", request.toString());
    }

    @Override
    public void onResponseSent(Response response) {
        if(response.isSuccess()) {
            int priority = response.getClass() == MonitorHostResponse.class? Log.DEBUG: Log.INFO;
            Timber.log(priority, "Complete serving, status is success, response is %s.", response.toString());
        } else {
            Timber.e(response.getThrowable(), "Complete serving, status is failed, response is %s.", response.toString());
        }
    }
}
