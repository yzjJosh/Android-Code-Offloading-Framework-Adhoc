package mobilecloud.engine;

import java.lang.reflect.Method;

/**
 * The cloud compute engine
 */
public class Engine {
    
    //Singleton instance
    private static Engine engine;
    
    //A flag to indicate if current environment is on cloud or not
    private static boolean onCloud = false;
    
    /**
     * Indicate that current environment is on cloud. Cloud environment should invoke 
     * this method before first execution.
     */
    public static void cloudInit() {
        onCloud = true;
    }
    
    /**
     * Determine if an invocation should be migrated to the cloud
     * @return true if should migrate
     */
    public boolean shouldMigrate(Method method, Object... args) {
        return !onCloud;
    }
    
    /**
     * Invoke this method on cloud
     * @return the result of this execution
     */
    public Object invokeRemotely(Method method, Object invoker, Object... args) {
       return null; 
    }
    
    /**
     * Get the singleton instance of Engine
     * @return The engine instance
     */
    public static Engine getInstance() {
        if(engine == null) {
            synchronized(Engine.class) {
                if(engine == null) {
                    engine = new Engine();
                }
            }
        }
        return engine;
    }
    
}
