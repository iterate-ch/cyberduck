package ch.cyberduck.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class SleepPreventerFactory extends Factory<SleepPreventer> {

    private static final Map<Platform, SleepPreventerFactory> factories
            = new HashMap<Platform, SleepPreventerFactory>();

    public static void addFactory(Factory.Platform platform, SleepPreventerFactory f) {
        factories.put(platform, f);
    }

    public static SleepPreventer get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return new DisabledSleepPreventer();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }

    private static final class DisabledSleepPreventer implements SleepPreventer {
        @Override
        public String lock() {
            return null;
        }

        @Override
        public void release(final String id) {
            //
        }
    }
}
