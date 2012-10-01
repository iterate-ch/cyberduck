package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;

/**
 * @version $Id:$
 */
public class DefaultCDNCredentialsIdentityConfiguration extends AbstractIdentityConfiguration {

    private Host host;

    public DefaultCDNCredentialsIdentityConfiguration(final Host host) {
        this.host = host;
    }

    @Override
    public Credentials getUserCredentials(final String username) {
        return host.getCdnCredentials();
    }
}
