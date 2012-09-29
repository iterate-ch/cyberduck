package ch.cyberduck.core.analytics;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

/**
 * @version $Id$
 */
public class QloudstatAnalyticsProvider implements AnalyticsProvider {
    private static Logger log = Logger.getLogger(QloudstatAnalyticsProvider.class);

    private static final String uri
            = Preferences.instance().getProperty("analytics.provider.qloudstat.setup");

    @Override
    public String getName() {
        return URI.create(uri).getHost();
    }

    @Override
    public String getSetup(final Protocol protocol, final String container,
                           final Credentials credentials) {
        if(!credentials.validate(protocol)) {
            return null;
        }
        final String setup = String.format("provider=%s,endpoint=%s,key=%s,secret=%s",
                protocol.getDefaultHostname(),
                container,
                credentials.getUsername(),
                credentials.getPassword());
        final String encoded;
        try {
            encoded = this.encode(new String(Base64.encodeBase64(setup.getBytes("UTF-8")), "UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return String.format("%s?setup=%s", uri, encoded);
    }

    private String encode(final String p) {
        try {
            StringBuilder b = new StringBuilder();
            b.append(URLEncoder.encode(p, "UTF-8"));
            // Becuase URLEncoder uses <code>application/x-www-form-urlencoded</code> we have to replace these
            // for proper URI percented encoding.
            return b.toString().replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}