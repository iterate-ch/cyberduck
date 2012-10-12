package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class IconServiceFactory extends Factory<IconService> {
    private static final Logger log = Logger.getLogger(IconServiceFactory.class);

    /**
     * Registered factories
     */
    protected static final Map<Platform, IconServiceFactory> factories
            = new HashMap<Platform, IconServiceFactory>();

    public static void addFactory(Factory.Platform platform, IconServiceFactory f) {
        factories.put(platform, f);
    }

    private static IconService service;

    public static IconService instance() {
        if(null == service) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                log.warn(String.format("No implementation for %s", NATIVE_PLATFORM));
                return new DisabledIconService();
            }
            service = factories.get(NATIVE_PLATFORM).create();
        }
        return service;
    }

    private static final class DisabledIconService implements IconService {
        @Override
        public boolean setIcon(final Local file, final String image) {
            return false;
        }

        @Override
        public boolean setProgress(final Local file, final int progress) {
            return false;
        }
    }
}