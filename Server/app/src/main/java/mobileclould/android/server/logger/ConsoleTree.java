package mobileclould.android.server.logger;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import com.jraska.console.Console;
import timber.log.Timber;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Log.*;
import static java.util.Locale.US;

public class ConsoleTree extends TaggedTree {

    private static final int PLACEHOLDER = 0;
    private static final int COLOR_VERBOSE = 0xff909090;
    private static final int COLOR_DEBUG = 0xffc88b48;
    private static final int COLOR_INFO = 0xffc9c9c9;
    private static final int COLOR_WARN = 0xffa97db6;
    private static final int COLOR_ERROR = 0xffff534e;
    private static final int COLOR_WTF = 0xffff5540;

    private static final int[] DEFAULT_COLORS = {PLACEHOLDER, PLACEHOLDER, COLOR_VERBOSE, COLOR_DEBUG,
            COLOR_INFO, COLOR_WARN, COLOR_ERROR, COLOR_WTF};
    private static final int REQUIRED_COLORS_LENGTH = DEFAULT_COLORS.length;
    private static final boolean DEFAULT_TAG_TEXT_BOLD = true;
    private static final String DEFAULT_TAG_FONT_FAMILY = "monospace";
    private static final String DEFAULT_MESSAGE_FONT_FAMILY = "serif";

    private static final int CALL_STACK_INDEX = 6;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");


    private final int minPriority;

    private final int[] priorityColorMapping;
    private final boolean tagTextBold;
    private final String tagFontFamily;
    private final String messageFontFamily;

    public ConsoleTree() {
        this(VERBOSE);
    }

    public ConsoleTree(int minPriority) {
        this(minPriority, DEFAULT_COLORS, DEFAULT_TAG_TEXT_BOLD, DEFAULT_TAG_FONT_FAMILY, DEFAULT_MESSAGE_FONT_FAMILY);
    }

    private ConsoleTree(int minPriority, int[] colors, boolean tagTextBold, String tagFontFamily, String messageFontFamily) {
        if (colors.length != REQUIRED_COLORS_LENGTH) {
            throw new IllegalArgumentException("Colors array must have length=" + REQUIRED_COLORS_LENGTH);
        }

        this.minPriority = minPriority;
        this.priorityColorMapping = colors;
        this.tagTextBold = tagTextBold;
        this.tagFontFamily = tagFontFamily;
        this.messageFontFamily = messageFontFamily;
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return priority >= minPriority;
    }

    @Override
    protected final void logWithTag(int priority, String tag, String message, Throwable t) {
        Console.writeLine(createSpannable(priority, tag, message));
    }

    private SpannableString createSpannable(int priority, String tag, String consoleMessage) {
        String prefix = null;
        if(tag == null) {
            prefix = String.format("%s", toPriorityString(priority));
        } else {
            prefix = String.format("%s/%s", toPriorityString(priority), tag);
        }
        int prefixLen = prefix.length();
        consoleMessage = String.format("%s: %s", prefix, consoleMessage);

        // Set the space character to non-breaking space
        consoleMessage = consoleMessage.replaceAll(" ", "\u00A0");

        SpannableString spannableString = new SpannableString(consoleMessage);

        // Set color
        spannableString.setSpan(new ForegroundColorSpan(priorityColorMapping[priority]), 0, consoleMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        // Bold tag
        if(tagTextBold) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, prefixLen, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        // Set tag font
        spannableString.setSpan(new TypefaceSpan(tagFontFamily), 0, prefixLen+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        // Set message font
        spannableString.setSpan(new TypefaceSpan(messageFontFamily), prefixLen+1, consoleMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private String toPriorityString(int priority) {
        switch (priority) {
            case ASSERT:
                return "WTF";
            case ERROR:
                return "E";
            case WARN:
                return "W";
            case INFO:
                return "I";
            case DEBUG:
                return "D";
            case VERBOSE:
                return "V";

            default:
                throw new IllegalArgumentException();
        }
    }

    public static final class Builder {
        private int minPriority = VERBOSE;
        private final int[] colors = Arrays.copyOf(DEFAULT_COLORS, REQUIRED_COLORS_LENGTH);
        private boolean tagTextBold = DEFAULT_TAG_TEXT_BOLD;
        private String tagFontFamily = DEFAULT_TAG_FONT_FAMILY;
        private String messageFontFamily = DEFAULT_MESSAGE_FONT_FAMILY;

        public Builder minPriority(int priority) {
            if (priority < VERBOSE || priority > ASSERT) {
                String message = String.format(US, "Priority %d is not in range <VERBOSE, ASSERT>(<%d,%d>)",
                        priority, VERBOSE, ASSERT);
                throw new IllegalArgumentException(message);
            }

            minPriority = priority;
            return this;
        }

        public Builder verboseColor(int color) {
            colors[VERBOSE] = color;
            return this;
        }

        public Builder debugColor(int color) {
            colors[DEBUG] = color;
            return this;
        }

        public Builder infoColor(int color) {
            colors[INFO] = color;
            return this;
        }

        public Builder warnColor(int color) {
            colors[WARN] = color;
            return this;
        }

        public Builder errorColor(int color) {
            colors[ERROR] = color;
            return this;
        }

        public Builder assertColor(int color) {
            colors[ASSERT] = color;
            return this;
        }

        public Builder tagTextBold(boolean tagTextBold) {
            this.tagTextBold = tagTextBold;
            return this;
        }

        public Builder tagFontFamily(String font) {
            this.tagFontFamily = font;
            return this;
        }

        public Builder messageFontFamily(String font) {
            this.messageFontFamily = font;
            return this;
        }

        public ConsoleTree build() {
            return new ConsoleTree(minPriority, Arrays.copyOf(colors, REQUIRED_COLORS_LENGTH), tagTextBold, tagFontFamily, messageFontFamily);
        }
    }
}

