package mobileclould.android.server.logger;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.util.Log.ASSERT;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

/**
 * A timber tree that logs to Android logcat
 */
public class LogCatTree extends Timber.Tree{

    private static final int CALL_STACK_INDEX = 6;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    private final int minPriority;

    public LogCatTree(int minPriority) {
        this.minPriority = minPriority;
    }

    @Override
    protected boolean isLoggable(int priority) {
        return priority >= minPriority;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if(tag == null) {
            tag = getTag();
        }

        switch (priority) {
            case ASSERT:
                Log.wtf(tag, message, t);
                break;
            case ERROR:
                Log.e(tag, message, t);
                break;
            case WARN:
                Log.w(tag, message, t);
                break;
            case INFO:
                Log.i(tag, message, t);
                break;
            case DEBUG:
                Log.d(tag, message, t);
                break;
            case VERBOSE:
                Log.v(tag, message, t);
                break;

            default:
                throw new IllegalArgumentException();
        }
    }

    private String createStackElementTag(StackTraceElement element) {
        String tag = element.getClassName();
        Matcher matcher = ANONYMOUS_CLASS.matcher(tag);
        if (matcher.find()) {
            tag = matcher.replaceAll("");
        }

        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    private String getTag() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length <= CALL_STACK_INDEX) {
            return null;
        }
        return createStackElementTag(stackTrace[CALL_STACK_INDEX]);
    }
}
