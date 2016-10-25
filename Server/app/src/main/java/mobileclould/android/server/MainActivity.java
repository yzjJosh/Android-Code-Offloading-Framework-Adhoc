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

import com.jraska.console.timber.ConsoleTree;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.ServerListener;
import mobileclould.android.server.logger.LogCatTree;
import mobileclould.android.server.logger.NonWordBreakConsoleTree;
import mobileclould.android.server.service.ServerService;
import timber.log.Timber;

public class MainActivity extends Activity implements ServiceConnection, ServerListener {

    private static final int CONSOLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int CONSOLE_TEXT_SIZE = 12;

    private ServerService service;

    // Initialize the Timber logger
    static {
        ConsoleTree consoleTree = new ConsoleTree.Builder().debugColor(Color.GRAY).
                infoColor(Color.BLACK).warnColor(Color.YELLOW).
                errorColor(Color.RED).build();
        Timber.plant(new NonWordBreakConsoleTree(consoleTree));
        Timber.plant(new LogCatTree(Log.VERBOSE));
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
  //      Timber.i("Start receiving request %s.", s);
    }

    @Override
    public void onRequestReceived(Request request) {
        Timber.i("Receive request %s.", request.getClass().getSimpleName());
    }

    @Override
    public void onResponseSent(Response response) {
        if(response.isSuccess()) {
            Timber.i("Complete serving, response type is %s, status is success.", response.getClass().getSimpleName());
        } else {
            Timber.e(response.getThrowable(), "Complete serving, response type is %s, status is failed.");
        }
    }
}
