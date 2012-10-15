package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class ApplicationLauncherFactory extends Factory<ApplicationLauncher> {
    private static final Logger log = Logger.getLogger(ApplicationLauncherFactory.class);

    /**
     * Registered factories
     */
    private static final Map<Factory.Platform, ApplicationLauncherFactory> factories
            = new HashMap<Factory.Platform, ApplicationLauncherFactory>();

    public static void addFactory(Factory.Platform platform, ApplicationLauncherFactory f) {
        factories.put(platform, f);
    }

    public static ApplicationLauncher get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            throw new FactoryException(String.format("No implementation for %s", NATIVE_PLATFORM));
        }
        return factories.get(NATIVE_PLATFORM).create();
    }
}
