package ch.cyberduck.core.s3;

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class S3HttpCredentialsTest {

    @Test
    public void testHttpCredentialsUrlProperty() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (Credentials from Instance Metadata).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());

        // Test that the profile's Context field is readable
        assertNotNull(host.getProtocol().getContext());
        assertEquals("http://169.254.169.254/latest/meta-data/iam/security-credentials/s3access",
                host.getProtocol().getContext());

        // Test that custom property can be set and retrieved
        host.setProperty("aws.credentials.http.url", "https://example.com/credentials");
        assertEquals("https://example.com/credentials", host.getProperty("aws.credentials.http.url"));
    }

    @Test
    public void testBookmarkWithHttpCredentialsUrl() throws Exception {
        final S3Protocol protocol = new S3Protocol();
        final Host host = new Host(protocol, "content-repo-prod-contentsupplier-touch.s3.amazonaws.com");

        // Simulate bookmark with HTTP credentials URL in Custom dict
        host.setProperty("aws.credentials.http.url",
                "https://up.example.com/VMHGTtd0BDYpcZgWU2arm6SwElXsiOCK");

        // Verify the property is accessible
        assertEquals("https://up.example.com/VMHGTtd0BDYpcZgWU2arm6SwElXsiOCK",
                host.getProperty("aws.credentials.http.url"));

        // Verify session can be created (without actually connecting)
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session);
    }
}
