package mobilecloud.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility functions for network programming
 *
 */
public class NetUtils {
    
    /**
     * Get local ip address
     * @return the ip address, or null if fails to get ip
     */
    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public static void main(String[] args) {
        System.out.println(getLocalIpAddress());
    }
}
