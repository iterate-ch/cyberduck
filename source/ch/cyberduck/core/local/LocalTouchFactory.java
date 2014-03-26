package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.local.features.Touch;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class LocalTouchFactory extends Factory<Touch> {

    /**
     * Registered factories
     */
    private static final Map<Platform, LocalTouchFactory> factories
            = new HashMap<Platform, LocalTouchFactory>();

    public static void addFactory(Factory.Platform platform, LocalTouchFactory f) {
        factories.put(platform, f);
    }

    public static Touch get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            return new DefaultLocalTouchFeature();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }
}
