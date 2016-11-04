package mobilecloud.server.register;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import mobilecloud.api.request.RegisterServerRequest;
import mobilecloud.client.Client;
import mobilecloud.engine.Config;
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
     * Initiate this register with a port number and a period
     * @param port the port number to be registered to central server
     * @param period the registering period
     */
    public Register(int port, long period) {
        this(new Host(Config.CENTRAL_SERVER_IP_ADDR, Config.CENTRAL_SERVER_PORT_NUMBER), port, period, Client.getInstance());
    }
    
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
    
    /**
     * Get the ip address of android device
     * @param useIPv4 use ip v4 or not
     * @return the ip address
     */
    private String getDeviceIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String sAddr = inetAddress.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                // drop ip6 port suffix
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    
    private class WorkerThread extends Thread {
        
        private boolean stopSign = false;
        
        @Override
        public void run() {
            while(!stopSign) {
                RegisterServerRequest req = new RegisterServerRequest();
                req.setServerIp(getDeviceIPAddress(true)).setServerPort(port).setIp(centralServer.ip)
                        .setPort(centralServer.port);
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
