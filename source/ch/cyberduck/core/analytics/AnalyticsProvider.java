package ch.cyberduck.core.analytics;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Protocol;

/**
 * @version $Id:$
 */
public interface AnalyticsProvider {

    String getName();

    String getSetup(Protocol protocol, String container,
                    Credentials credentials);
}
