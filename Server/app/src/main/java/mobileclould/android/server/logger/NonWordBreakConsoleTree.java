package mobileclould.android.server.logger;

import com.jraska.console.timber.ConsoleTree;

import timber.log.Timber;

public class NonWordBreakConsoleTree extends Timber.Tree{

    private final ConsoleTree tree;

    public NonWordBreakConsoleTree(ConsoleTree tree) {
        this.tree = tree;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {}

    private String transfer(String input) {
        if(input == null) {
            return null;
        } else {
            return input.replaceAll(" ", "\u00A0");
        }
    }

    @Override
    public void v(String message, Object... args) {
        tree.v(transfer(message), args);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        tree.v(t, transfer(message), args);
    }

    @Override
    public void d(String message, Object... args) {
        tree.d(transfer(message), args);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        tree.d(t, transfer(message), args);
    }

    @Override
    public void i(String message, Object... args) {
        tree.i(transfer(message), args);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        tree.i(t, transfer(message), args);
    }

    @Override
    public void w(String message, Object... args) {
        tree.w(transfer(message), args);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        tree.w(t, transfer(message), args);
    }

    @Override
    public void e(String message, Object... args) {
        tree.e(transfer(message), args);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        tree.e(t, transfer(message), args);
    }

    @Override
    public void wtf(String message, Object... args) {
        tree.wtf(transfer(message), args);
    }

    @Override
    public void wtf(Throwable t, String message, Object... args) {
        tree.wtf(t, transfer(message), args);
    }

    @Override
    public void log(int priority, String message, Object... args) {
        tree.log(priority, transfer(message), args);
    }

    @Override
    public void log(int priority, Throwable t, String message, Object... args) {
        tree.log(priority, t, transfer(message), args);
    }
}
