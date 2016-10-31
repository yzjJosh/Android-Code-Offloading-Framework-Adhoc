package mobileclould.android.server.service;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobilecloud.server.Server;
import mobilecloud.server.ServerListener;
import timber.log.Timber;

public class ServerThread extends Thread {

    private final int port;
    private ServerListener listener;
    private ServerSocket serverSocket;
    private boolean stopSign;

    public ServerThread(int port) {
        this.port = port;
        this.stopSign = false;
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Timber.e(e);
        }
        if(serverSocket != null) {
            try {
                Timber.i("Server thread starts, waiting for requests on port %d...", port);
                while (!stopSign) {
                    Socket socket = serverSocket.accept();
                    Work worker = new Work(socket);
                    executor.submit(worker);
                }
            } catch (IOException e) {
                Timber.d(e);
            }
        }
        executor.shutdown();
        Timber.w("Server thread is stopped.");
    }

    public void registerServerListener(ServerListener listener) {
        this.listener = listener;
    }

    public void kill() {
        stopSign = true;
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

    private class Work implements Runnable {

        private final Socket socket;

        public Work(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                Server.getInstance().serve(is, os, listener);

            } catch (Throwable e) {
                Timber.e(e, "Error occurs when serving.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    Timber.e(e, "Error occurs when closing socket.");
                }
            }
        }
    }
}