package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
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
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.utils.SignatureUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
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
                this.getClass().getResourceAsStream("/S3 (HTTP).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
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
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testAWS2SignatureVersion() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 AWS2 Signature Version (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnectSessionTokenStatic() throws Exception {
        final S3Protocol protocol = new S3Protocol();
        final Host host = new Host(protocol, protocol.getDefaultHostname(), new Credentials()
                .withTokens(new TemporaryAccessTokens(
                        "ASIA5RMYTHDIR37CTCXI",
                        "TsnhChH4FlBt7hql2KnzrwNizmktJnO8YzDQwFqx",
                        "FQoDYXdzEN3//////////wEaDLAz85HLZTQ7zu6/OSKrAfwLewUMHKaswh5sXv50BgMwbeKfCoMATjagvM+KV9++z0I6rItmMectuYoEGCOcnWHKZxtvpZAGcjlvgEDPw1KRYu16riUnd2Yo3doskqAoH0dlL2nH0eoj0d81H5e6IjdlGCm1E3K3zQPFLfMbvn1tdDQR1HV8o9eslmxo54hWMY2M14EpZhcXQMlns0mfYLYHLEVvgpz/8xYjR0yKDxJlXSATEpXtowHtqSi8tL7aBQ==",
                        -1L
                )));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertThrows(ExpiredTokenException.class, () -> session.login(new DisabledLoginCallback(), new DisabledCancelCallback()));
    }

    @Test
    public void testConnectDefaultPath() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        host.setDefaultPath("/test-eu-west-1-cyberduck");
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = BackgroundException.class)
    public void testCustomHostnameUnknown() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        final Host host = new Host(profile, "testu.cyberduck.ch", new Credentials(
                PROPERTIES.get("s3.key"), "s"
        ));
        final S3Session session = new S3Session(host);
        try {
            session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(BackgroundException e) {
            assertTrue(e.getCause() instanceof UnknownHostException);
            throw e;
        }
    }

    @Test
    public void testCustomHostname() throws Exception {
        final Host host = new Host(new S3Protocol(), "cyberduck.io", new Credentials(
                PROPERTIES.get("s3.key"), "s"
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
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(set.get());
    }

    @Test
    public void testFeatures() {
        final S3Session aws = new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname()));
        assertNotNull(aws.getFeature(Copy.class));
        assertNotNull(aws.getFeature(AclPermission.class));
        assertNotNull(aws.getFeature(Versioning.class));
        assertNotNull(aws.getFeature(Lifecycle.class));
        assertNotNull(aws.getFeature(Location.class));
        assertNotNull(aws.getFeature(Encryption.class));
        assertNotNull(aws.getFeature(Redundancy.class));
        assertNotNull(aws.getFeature(Logging.class));
        assertNotNull(aws.getFeature(DistributionConfiguration.class));
        assertEquals(S3ThresholdDeleteFeature.class, aws.getFeature(Delete.class).getClass());
        final S3Session o = new S3Session(new Host(new S3Protocol(), "o"));
        assertNotNull(o.getFeature(Copy.class));
        assertNotNull(o.getFeature(AclPermission.class));
        assertNotNull(o.getFeature(Versioning.class));
        assertNotNull(o.getFeature(Lifecycle.class));
        assertNotNull(o.getFeature(Location.class));
        assertNull(o.getFeature(Encryption.class));
        assertNotNull(o.getFeature(Redundancy.class));
        assertNotNull(o.getFeature(Logging.class));
        assertNotNull(o.getFeature(DistributionConfiguration.class));
        assertEquals(S3DefaultDeleteFeature.class, o.getFeature(Delete.class).getClass());
    }

    @Test
    public void testBucketVirtualHostStyleCustomHost() throws Exception {
        final Host host = new Host(new S3Protocol(), "test-eu-central-1-cyberduck");
        assertFalse(new S3Session(host).connect(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback())
                .getDisableDnsBuckets());
    }

    @Test
    public void testBucketVirtualHostStyleAmazon() throws Exception {
        final Host host = new Host(new S3Protocol(), "test-eu-central-1-cyberduck.s3.amazonaws.com", new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        assertFalse(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback())
                .getDisableDnsBuckets());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test
    public void testTrustChain() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                PROPERTIES.get("s3.key"), PROPERTIES.get("s3.secret")
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
        c.connect(session, new DisabledCancelCallback());
        assertTrue(verified.get());
        session.close();
    }

    @Test
    public void testInteroperabilityMinio() throws Exception {
        final Host host = new Host(new S3Protocol(), "play.min.io", new Credentials(
                "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.bucket.virtualhost.disable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectCn_North_1() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.cn-north-1.amazonaws.com.cn", new Credentials("AWS-QWEZUKJHGVCVBJHG", "uztfjkjnbvcf"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test
    public void testAwsHostnames() {
        assertFalse(S3Session.isAwsHostname("play.min.io"));
        assertTrue(S3Session.isAwsHostname("test-eu-west-3-cyberduck.s3.amazonaws.com"));
        assertTrue(S3Session.isAwsHostname("s3.dualstack.eu-west-3.amazonaws.com"));
        assertTrue(S3Session.isAwsHostname("test-eu-west-3-cyberduck.s3.dualstack.eu-west-3.amazonaws.com"));
        assertTrue(S3Session.isAwsHostname("s3.amazonaws.com"));
        assertTrue(S3Session.isAwsHostname("s3.amazonaws.com.cn"));
        assertFalse(S3Session.isAwsHostname("s3.amazonaws.com.cn", false));
        assertTrue(S3Session.isAwsHostname("s3.cn-north-1.amazonaws.com.cn"));
        assertFalse(S3Session.isAwsHostname("s3.cn-north-1.amazonaws.com.cn", false));
        assertTrue(S3Session.isAwsHostname("vpce-0971cacd1f2.s3.eu-west-1.vpce.amazonaws.com"));
    }

    @Test
    public void testVpcHostname() {
        assertEquals("eu-west-1", SignatureUtils.awsRegionForRequest(URI.create("https://vpce-1.s3.eu-west-1.vpce.amazonaws.com")));
    }
}
