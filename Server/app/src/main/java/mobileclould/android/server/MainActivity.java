package mobileclould.android.server;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
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

public class MainActivity extends Activity implements ServiceConnection, ServerListener, CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener{

    private static final int CONSOLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int CONSOLE_TEXT_SIZE = 13;
    private static final String[] LOG_LEVEL_DESCRIPTION = new String[]{"Verbose", "Debug", "Info", "Warn", "Error", "Assert"};

    private static ConsoleTree consoleTree;

    private ServerService service;
    private Switch serverSwitch;

    // Initialize the Timber logger
    static {
        consoleTree = new ConsoleTree.Builder()
                .verboseColor(Color.DKGRAY).debugColor(Color.GRAY).
                infoColor(Color.BLACK).warnColor(Color.rgb(234, 187, 46)).
                errorColor(Color.RED).assertColor(Color.CYAN)
                .minPriority(Log.INFO)
                .build();
        Timber.plant(consoleTree);
        Timber.plant(new LogCatTree(Log.INFO));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConsole();

        //Bind service firstly
        bindService(new Intent(this, ServerService.class), this, BIND_AUTO_CREATE);

        // Init switch
        serverSwitch = (Switch) findViewById(R.id.server_switch);
        serverSwitch.setOnCheckedChangeListener(this);

        // Init log spinner
        Spinner logLevelSpinner = (Spinner) findViewById(R.id.log_level_spanner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, LOG_LEVEL_DESCRIPTION);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        logLevelSpinner.setAdapter(adapter);
        logLevelSpinner.setOnItemSelectedListener(this);
        logLevelSpinner.setSelection(consoleTree.getMinPriority()-Log.VERBOSE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind service when stops
        unbindService(this);
    }

    // Initialize the console
    private void initConsole() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.console_scroll_view);
        scrollView.setBackgroundColor(CONSOLE_BACKGROUND_COLOR);
        TextView textView = (TextView) findViewById(R.id.console_text);
        textView.setTextSize(CONSOLE_TEXT_SIZE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((ServerService.ServiceBinder) service).getService();
        this.serverSwitch.setChecked(this.service.isStarted());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Timber.wtf("Service connection is lost!");
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
    public void onResponseSent(Request req, Response response) {
        if(response.isSuccess()) {
            int priority = response.getClass() == MonitorHostResponse.class? Log.DEBUG: Log.INFO;
            Timber.log(priority, "Complete serving %s, status is success, response is %s.", req.getClass().getSimpleName(), response.toString());
        } else {
            Timber.e(response.getThrowable(), "Complete serving %s, status is failed, response is %s.", req.getClass().getSimpleName(), response.toString());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            if(!service.isStarted()) {
                // Make this service long time alive
                startService(new Intent(this, ServerService.class));
                // Start the server
                service.startServer(Config.PORT_NUMBER);
                service.registerServerListener(this);
            }
        } else {
            if(service.isStarted()) {
                service.stopServer();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        consoleTree.setMinPriority(position + Log.VERBOSE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
