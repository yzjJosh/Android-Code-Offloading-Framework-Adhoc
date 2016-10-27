package mobileclould.android.server.logger;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.util.Log.*;

/**
 * A timber tree that logs to Android logcat
 */
public class LogCatTree extends TaggedTree{

    private final int minPriority;

    public LogCatTree() {
        this(VERBOSE);
    }

    public LogCatTree(int minPriority) {
        this.minPriority = minPriority;
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return priority >= minPriority;
    }

    @Override
    protected void logWithTag(int priority, String tag, String message, Throwable t) {
        if(tag == null) {
            tag = "";
        }
        switch (priority) {
            case ASSERT:
                Log.wtf(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
            case WARN:
                Log.w(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case DEBUG:
                Log.d(tag, message);
                break;
            case VERBOSE:
                Log.v(tag, message);
                break;

            default:
                throw new IllegalArgumentException();
        }
    }

}
