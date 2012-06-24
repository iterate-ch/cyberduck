package ch.cyberduck.core.analytics;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * @version $Id$
 */
public class QloudstatAnalyticsProvider implements AnalyticsProvider {

    private static final String uri
            = Preferences.instance().getProperty("analytics.provider.qloudstat.setup");

    @Override
    public String getName() {
        return URI.create(uri).getHost();
    }

    @Override
    public String getSetup(final Protocol protocol, final String container,
                           final Credentials credentials) {
        if(null == credentials) {
            return null;
        }
        final String setup = String.format("provider=%s,endpoint=%s,key=%s,secret=%s",
                protocol.getDefaultHostname(),
                container,
                credentials.getUsername(),
                credentials.getPassword());
        final String encoded;
        try {
            encoded = Path.encode(new String(Base64.encodeBase64(setup.getBytes("UTF-8")), "UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return String.format("%s?setup=%s", uri, encoded);
    }
}