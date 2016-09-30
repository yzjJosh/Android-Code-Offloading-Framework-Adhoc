package mobilecloud.engine;

/**
 * Schedular is responsible to schedule which host to execute a task
 *
 */
public class Schedular {
    
    private static Schedular instance;
    
    // TODO
    public Host schedule() {
        return null;
    }
    
    //TODO
    public int availableNum() {
        return 0;
    }
    
    public boolean haveAvailable() {
        return availableNum() > 0;
    }
    
    public static Schedular getInstance() {
        if(instance == null) {
            synchronized(Schedular.class) {
                if(instance == null) {
                    instance = new Schedular();
                }
            }
        }
        return instance;
    }

}
