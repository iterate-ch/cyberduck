package ch.cyberduck.core.editor;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class ApplicationFinderFactory extends Factory<ApplicationFinder> {

    /**
     * Registered factories
     */
    protected static final Map<Factory.Platform, ApplicationFinderFactory> factories
            = new HashMap<Factory.Platform, ApplicationFinderFactory>();

    public static void addFactory(Factory.Platform platform, ApplicationFinderFactory f) {
        factories.put(platform, f);
    }

    private static ApplicationFinder finder;

    public static ApplicationFinder instance() {
        if(null == finder) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new FactoryException(String.format("No implementation for %s", NATIVE_PLATFORM));
            }
            finder = factories.get(NATIVE_PLATFORM).create();
        }
        return finder;
    }
}