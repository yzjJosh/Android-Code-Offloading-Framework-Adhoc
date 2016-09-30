package mobilecloud.lib;

import java.io.Serializable;

/**
 * An interface which can access meta data of remotable objects
 */
public interface Remotable extends Serializable {
    
    public void setIsOnServer(boolean val);
    
    public boolean isOnServer();
    
    public int getId();
    
    public void setId(int id);
    
    public boolean isNew();
    
    public void setIsNew(boolean val);
}
