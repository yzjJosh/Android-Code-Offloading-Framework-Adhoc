package mobileclould.android.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import mobilecloud.server.Server;
import mobilecloud.server.ServerListener;
import mobilecloud.server.register.Register;
import mobileclould.android.server.Config;

public class ServerService extends Service {

    private ServerThread thread;
    private Register register;
    private IBinder binder = new ServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Start the server on given port number
     */
    public synchronized void startServer(int port) {
        if(thread == null) {
            Server.init(this);
            register = new Register(Config.PORT_NUMBER, Config.CENTRAL_SERVER_REGISTER_PERIOD);
            register.start();
            thread = new ServerThread(port);
            thread.start();
        } else {
            throw new IllegalStateException("Server is already started!");
        }
    }

    /**
     * Stop the server
     */
    public synchronized void stopServer() {
        if(thread != null) {
            register.stop();
            thread.kill();
            thread = null;
        }
    }

    /**
     * Check if the server is started
     * @return true if started
     */
    public synchronized boolean isStarted() {
        return thread != null;
    }

    /**
     * Register a server listener to the server
     * @param listener the listener to monitor the server
     */
    public synchronized void registerServerListener(ServerListener listener) {
        if(thread != null) {
            thread.registerServerListener(listener);
        }
    }



    public class ServiceBinder extends Binder {

        public ServerService getService() {
            return ServerService.this;
        }

    }
}