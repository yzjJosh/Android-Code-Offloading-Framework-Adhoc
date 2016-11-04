package mobilecloud.engine;

import mobilecloud.engine.host.provider.CentralServerHostProvider;
import mobilecloud.engine.schedular.RoundRobinSchedular;

public class Config {
    
    public static final String SCHEDULAR = RoundRobinSchedular.class.getName();
    public static final String HOST_PROVIDER = CentralServerHostProvider.class.getName();
    public static final long HOST_MONITOR_CHECK_PROVIDER_INTERVAL = 10000;
    public static final long HOST_MONITOR_CHECK_HOST_INTERVAL = 5000;
    public static final int HOST_MONITOR_RETRY_TIMES = 3;
    public static final long HOST_MONITOR_RETRY_INTERVAL = 1000;
    public static final String CENTRAL_SERVER_IP_ADDR = "54.67.59.160";
    public static final int CENTRAL_SERVER_PORT_NUMBER = 2048;
    public static final String EXECUTABLE_FOLDER = "exe";
    public static final String TEMP_OUTPUT_FOLDER = "tmp";
    public static final String EXECUTABLE_NAME = "executable.zip";
}
