package mobileclould.android.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import mobilecloud.server.Server;
import mobileclould.android.server.Config;

public class ServerService extends Service {

    public static final String PORT_NUMBER_KEY = "PORT_NUMBER";

    private ServerThread thread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && thread == null) {
            // Init the server firstly
            Server.init(this);

            // Create a server thread
            int port = intent.getIntExtra(PORT_NUMBER_KEY, Config.PORT_NUMBER);
            thread = new ServerThread((port));
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}