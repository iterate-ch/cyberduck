package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3SessionTest extends AbstractS3Test {

    @Test
    public void testHttpProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (HTTP).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        assertFalse(host.getProtocol().isSecure());
        final S3Session session = new S3Session(host, new X509TrustManager() {
            @Override
            public X509TrustManager init() {
                return this;
            }

            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                throw new CertificateException();
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, final String cipher) {
                fail();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, final String cipher) {
                fail();
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                fail();
                return null;
            }
        }, new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = ExpiredTokenException.class)
    public void testConnectSessionTokenStatic() throws Exception {
        final S3Protocol protocol = new S3Protocol() {
            @Override
            public boolean isTokenConfigurable() {
                return true;
            }
        };
        final Host host = new Host(protocol, protocol.getDefaultHostname(), new Credentials(
            "ASIA5RMYTHDIR37CTCXI", "TsnhChH4FlBt7hql2KnzrwNizmktJnO8YzDQwFqx",
            "FQoDYXdzEN3//////////wEaDLAz85HLZTQ7zu6/OSKrAfwLewUMHKaswh5sXv50BgMwbeKfCoMATjagvM+KV9++z0I6rItmMectuYoEGCOcnWHKZxtvpZAGcjlvgEDPw1KRYu16riUnd2Yo3doskqAoH0dlL2nH0eoj0d81H5e6IjdlGCm1E3K3zQPFLfMbvn1tdDQR1HV8o9eslmxo54hWMY2M14EpZhcXQMlns0mfYLYHLEVvgpz/8xYjR0yKDxJlXSATEpXtowHtqSi8tL7aBQ=="
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test
    public void testConnectSessionTokenFromService() throws Exception {
        final S3Protocol protocol = new S3Protocol() {
            @Override
            public boolean isTokenConfigurable() {
                return true;
            }
        };
        final Host host = new Host(protocol, protocol.getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnectDefaultPath() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        host.setDefaultPath("/test-us-east-1-cyberduck");
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testCustomHostnameUnknown() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, "testu.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("s3.key"), "s"
        ));
        final S3Session session = new S3Session(host);
        try {
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(BackgroundException e) {
            assertTrue(e.getCause() instanceof UnknownHostException);
            throw e;
        }
    }

    @Test(expected = BackgroundException.class)
    public void testCustomHostname() throws Exception {
        final Host host = new Host(new S3Protocol(), "cyberduck.io", new Credentials(
            System.getProperties().getProperty("s3.key"), "s"
        ));
        final AtomicBoolean set = new AtomicBoolean();
        final S3Session session = new S3Session(host);
        session.withListener(new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                switch(request) {
                    case request:
                        if(message.contains("Host:")) {
                            assertEquals("Host: cyberduck.io", message);
                            set.set(true);
                        }
                }
            }
        });
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        try {
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(BackgroundException e) {
            assertTrue(set.get());
            throw e;
        }
        fail();
    }

    @Test
    public void testFeatures() {
        final S3Session aws = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        assertNotNull(aws.getFeature(Copy.class));
        assertNotNull(aws.getFeature(AclPermission.class));
        assertNotNull(aws.getFeature(Versioning.class));
        assertNotNull(aws.getFeature(AnalyticsProvider.class));
        assertNotNull(aws.getFeature(Lifecycle.class));
        assertNotNull(aws.getFeature(Location.class));
        assertNotNull(aws.getFeature(Encryption.class));
        assertNotNull(aws.getFeature(Redundancy.class));
        assertNotNull(aws.getFeature(Logging.class));
        assertNotNull(aws.getFeature(DistributionConfiguration.class));
        assertNotNull(aws.getFeature(IdentityConfiguration.class));
        assertNotNull(aws.getFeature(IdentityConfiguration.class));
        assertEquals(S3MultipleDeleteFeature.class, aws.getFeature(Delete.class).getClass());
        final S3Session o = new S3Session(new Host(new S3Protocol(), "o"));
        assertNotNull(o.getFeature(Copy.class));
        assertNotNull(o.getFeature(AclPermission.class));
        assertNotNull(o.getFeature(Versioning.class));
        assertNull(o.getFeature(AnalyticsProvider.class));
        assertNotNull(o.getFeature(Lifecycle.class));
        assertNotNull(o.getFeature(Location.class));
        assertNull(o.getFeature(Encryption.class));
        assertNotNull(o.getFeature(Redundancy.class));
        assertNotNull(o.getFeature(Logging.class));
        assertNotNull(o.getFeature(DistributionConfiguration.class));
        assertNull(o.getFeature(IdentityConfiguration.class));
        assertEquals(S3DefaultDeleteFeature.class, o.getFeature(Delete.class).getClass());
    }

    @Test
    public void testBucketVirtualHostStyleCustomHost() {
        final Host host = new Host(new S3Protocol(), "test-us-east-1-cyberduck");
        assertFalse(new S3Session(host).configure().getBoolProperty("s3service.disable-dns-buckets", true));
    }

    @Test
    public void testBucketVirtualHostStyleAmazon() {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname());
        assertFalse(new S3Session(host).configure().getBoolProperty("s3service.disable-dns-buckets", true));
    }

    @Test
    public void testBucketVirtualHostStyleEucalyptusDefaultHost() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/Eucalyptus Walrus S3.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        assertFalse(new S3Session(host).configure().getBoolProperty("s3service.disable-dns-buckets", false));
    }

    @Test
    public void testBucketVirtualHostStyleEucalyptusCustomDeployment() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/Eucalyptus Walrus S3.cyberduckprofile"));
        final Host host = new Host(profile, "ec.cyberduck.io");
        assertFalse(new S3Session(host).configure().getBoolProperty("s3service.disable-dns-buckets", false));
    }

    @Test(expected = LoginFailureException.class)
    @Ignore
    public void testTemporaryAccessToken() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile"));
        assertTrue(profile.validate(new Credentials(), new LoginOptions(profile)));
        final Host host = new Host(profile);
        final S3Session s = new S3Session(host);
        s.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        try {
            s.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(LoginFailureException e) {
            assertEquals(ConnectionTimeoutException.class, e.getCause().getClass());
            throw e;
        }
    }

    @Test
    public void testTrustChain() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final AtomicBoolean verified = new AtomicBoolean();
        final S3Session session = new S3Session(host, new DefaultX509TrustManager() {
            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                verified.set(true);
                super.verify(hostname, certs, cipher);
            }
        },
            new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore()));
        final LoginConnectionService c = new LoginConnectionService(
            new DisabledLoginCallback(),
            new DisabledHostKeyCallback(),
            new DisabledPasswordStore(),
            new DisabledProgressListener()
        );
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(verified.get());
        session.close();
    }

    @Test
    @Ignore
    public void testInteroperabilityMinio() throws Exception {
        final Host host = new Host(new S3Protocol(), "play.minio.io", 9000, new Credentials(
            "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
        ));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testConnectCn_North_1MissingToken() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.cn-north-1.amazonaws.com.cn");
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectCn_North_1() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.cn-north-1.amazonaws.com.cn", new Credentials("AWS-QWEZUKJHGVCVBJHG", "uztfjkjnbvcf"));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }
}
