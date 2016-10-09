package mobilecloud.engine;

//import mobilecloud.engine.host.provider.FileHostProvider;
import mobilecloud.engine.host.provider.StaticHostProvider;
import mobilecloud.engine.schedular.RoundRobinSchedular;

public class Config {
    
    public static final String SCHEDULAR = RoundRobinSchedular.class.getName();
    public static final String HOST_PROVIDER = StaticHostProvider.class.getName();
    public static final long HOST_MONITOR_CHECK_PROVIDER_INTERVAL = 10000;
    public static final long HOST_MONITOR_CHECK_HOST_INTERVAL = 5000;
}
