package example.helloword;

import android.util.Log;

import java.lang.reflect.Method;

import mobilecloud.engine.Engine;
import mobilecloud.lib.Remotable;
import mobilecloud.lib.Remote;

public class RemoteObject implements Remotable {

    private final String TAG = getClass().getSimpleName();

    private int id;
    private boolean onServer;
    private boolean isNew;

    @Override
    public void setIsOnServer(boolean b) {
        this.onServer = b;
    }

    @Override
    public boolean isOnServer() {
        return this.onServer;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int i) {
        this.id = i;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @Override
    public void setIsNew(boolean b) {
        this.isNew = b;
    }

    @Remote
    public int add(int a, int b) {
        try {
            Method method = getClass().getMethod("add", int.class, int.class);
            if(Engine.getInstance().shouldMigrate(method, this, a, b)) {
                Log.d(TAG, "Start calculating remotely ...");
                return (Integer) Engine.getInstance().invokeRemotely(method, this, a, b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Calculating locally ...");
        return a + b;
    }
}
