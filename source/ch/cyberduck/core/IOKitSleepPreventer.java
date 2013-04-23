package ch.cyberduck.core;

import ch.cyberduck.core.library.Native;

/**
 * @version $Id:$
 */
public class IOKitSleepPreventer implements SleepPreventer {

    public static void register() {
        SleepPreventerFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends SleepPreventerFactory {
        @Override
        protected SleepPreventer create() {
            return new IOKitSleepPreventer();
        }
    }

    static {
        Native.load("IOKitSleepPreventer");
    }

    private IOKitSleepPreventer() {
        //
    }

    private static final Object lock = new Object();

    @Override
    public String lock() {
        synchronized(lock) {
            return this.createAssertion(Preferences.instance().getProperty("application.name"));
        }
    }

    private native String createAssertion(String reason);

    @Override
    public void release(final String id) {
        synchronized(lock) {
            this.releaseAssertion(id);
        }
    }

    private native void releaseAssertion(String id);
}
