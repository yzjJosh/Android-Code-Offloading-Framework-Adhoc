package mobilecloud.server.register;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.client.Client;
import mobilecloud.engine.host.Host;

/**
 * Register is a component that periodically register this server's IP and port
 * number to the central server
 */
public class Register {
    
    private final Host centralServer;
    private final int port;
    private final long period;
    private final Client client;
    private WorkerThread thread;
    
    /**
     * Initiate this register with a port number
     * @param centralServer the central server host
     * @param port the port number to be registered to central server
     * @param period the registering period
     * @param client the client which can send request to central server
     */
    public Register(Host centralServer, int port, long period, Client client) {
        this.centralServer = centralServer;
        this.port = port;
        this.period = period;
        this.client = client;
    }
    
    /**
     * Start registering periodically
     */
    public synchronized void start() {
        if(thread == null) {
            thread = new WorkerThread();
            thread.start();
        }
    }
    
    /**
     * Stop registering this server
     */
    public synchronized void stop() {
        if(thread != null) {
            thread.kill();
            thread = null;
        }
    }
    
    private class WorkerThread extends Thread {
        
        private boolean stopSign = false;
        
        @Override
        public void run() {
            while(!stopSign) {
                RegisterServerRequest req = new RegisterServerRequest();
                req.setServerPort(port).setIp(centralServer.ip).setPort(centralServer.port);
                try {
                    client.request(req);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(period);
                } catch(InterruptedException e) {}
            }
        }
        
        // Kill this thread
        public void kill() {
            interrupt();
            stopSign = true;
        }
        
    }
    
}
