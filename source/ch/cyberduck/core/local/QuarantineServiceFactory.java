package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class QuarantineServiceFactory extends Factory<QuarantineService> {
    private static final Logger log = Logger.getLogger(QuarantineServiceFactory.class);

    /**
     * Registered factories
     */
    private static final Map<Factory.Platform, QuarantineServiceFactory> factories
            = new HashMap<Factory.Platform, QuarantineServiceFactory>();

    public static void addFactory(Factory.Platform platform, QuarantineServiceFactory f) {
        factories.put(platform, f);
    }

    public static QuarantineService get() {
        if(!factories.containsKey(NATIVE_PLATFORM)) {
            log.warn(String.format("No implementation for %s", NATIVE_PLATFORM));
            return new DisabledQuarantineService();
        }
        return factories.get(NATIVE_PLATFORM).create();
    }

    private static final class DisabledQuarantineService implements QuarantineService {
        @Override
        public void setQuarantine(final Local file, final String originUrl, final String dataUrl) {
            //
        }

        @Override
        public void setWhereFrom(final Local file, final String dataUrl) {
            //
        }
    }
}
