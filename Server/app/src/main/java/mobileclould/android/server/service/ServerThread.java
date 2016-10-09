package mobileclould.android.server.service;

import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobilecloud.api.Request;
import mobilecloud.api.Response;
import mobilecloud.server.Server;

public class ServerThread extends Thread {

    private final String TAG = getClass().getSimpleName();

    private final int port;
    private final ExecutorService executor;
    private boolean stopSign;

    public ServerThread(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
        this.stopSign = false;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Log.i(TAG, "Server thread starts, waiting for requests on port " + port);
            while(!stopSign) {
                Socket socket = serverSocket.accept();
                Work worker = new Work(socket);
                executor.submit(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    public void kill() throws InterruptedException {
        Log.i(TAG, "Killing server thread ...");
        stopSign = true;
        interrupt();
        join();
    }

    private class Work implements Callable<Response> {

        private final String TAG = getClass().getSimpleName();
        private final Socket socket;

        public Work(Socket socket) {
            this.socket = socket;
        }

        @Override
        public Response call() throws Exception {
            try {
                ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                Request request = (Request) is.readObject();
                Log.d(TAG, "Got request type: " + request.getClass().getSimpleName());
                Response resp = Server.getInstance().serve(request);
                if(resp.isSuccess()) {
                    Log.d(TAG, "Complete serving request " + request.getClass().getSimpleName() +
                            ", status is success.");
                } else {
                    Log.d(TAG, "Compelte serving request" + request.getClass().getSimpleName() +
                            ", status is failed, reason is " + resp.getThrowable());
                }
                os.writeObject(resp);
                os.flush();
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                socket.close();
            }
        }
    }
}