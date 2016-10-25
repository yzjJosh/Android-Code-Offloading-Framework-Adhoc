package mobileclould.android.server.logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 *  Tagged tree is a tree that automatically generate tags based on current stack trace
 */
public abstract class TaggedTree extends Timber.Tree{

    private static final int CALL_STACK_INDEX = 6;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (tag == null) {
            tag = getTag();
        }
        logWithTag(priority, tag, message, t);
    }

    /**
     * Log a message with given priority, tag, message, and t. The tag may be generated automatically, but it could be null
     * if auto-genrating tag fails.
     * @param priority the priority
     * @param tag the tag
     * @param message the message
     * @param t the error
     */
    protected abstract void logWithTag(int priority, String tag, String message, Throwable t);

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
