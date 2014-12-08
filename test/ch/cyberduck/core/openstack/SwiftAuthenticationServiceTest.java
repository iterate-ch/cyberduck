package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.method.Authentication10UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20AccessKeySecretKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20RAXUsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class SwiftAuthenticationServiceTest extends AbstractTestCase {

    @Test
    public void testGetRequest() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v1.0");
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials("u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(new SwiftProtocol(), "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(new SwiftProtocol(), "myhost", new Credentials("u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(new SwiftProtocol(), "myhost", new Credentials("u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals("GET", s.getRequest(new Host(new SwiftProtocol(), "myhost", new Credentials("u", "P")),
                new DisabledLoginCallback()).iterator().next().getMethod());
        assertEquals("POST", s.getRequest(new Host(new SwiftProtocol(), "lon.identity.api.rackspacecloud.com", new Credentials("u", "P")),
                new DisabledLoginCallback()).iterator().next().getMethod());
        final Host host = new Host(new SwiftProtocol(), "identity.openstack.com", new Credentials("u", "P"));
        host.setPort(3451);
        assertEquals(URI.create("https://identity.openstack.com:3451/v1.0"), s.getRequest(host, new DisabledLoginCallback()).iterator().next().getURI());
        assertEquals(Client.AuthVersion.v10, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Authentication10UsernameKeyRequest.class, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getClass());
    }

    @Test
    public void testGetDefault2() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(new SwiftProtocol(), "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("tenant:u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(new SwiftProtocol(), "myhost", new Credentials("tenant:u", "P")),
                        new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Authentication20UsernamePasswordRequest.class,
                new ArrayList<AuthenticationRequest>(s.getRequest(new Host(new SwiftProtocol(), "myhost", new Credentials("tenant:u", "P")),
                        new DisabledLoginCallback())).get(0).getClass());
    }

    @Test(expected = LoginCanceledException.class)
    public void testGetDefault2NoTenant() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        final Credentials credentials = new Credentials("u", "P");
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(new SwiftProtocol(), "region-b.geo-1.identity.hpcloudsvc.com", credentials),
                        new DisabledLoginCallback()).iterator().next().getVersion());
    }

    @Test
    public void testGetDefault2EmptyTenant() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        final Host host = new Host(new SwiftProtocol(), "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("u", "P"));
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(host,
                        new DisabledLoginCallback() {
                            @Override
                            public void prompt(final Protocol protocol, final Credentials credentials,
                                               final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                                credentials.setUsername("");
                            }
                        }).iterator().next().getVersion());
        assertEquals(":u", host.getCredentials().getUsername());
    }

    @Test
    public void testProfileHPCloud() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("profiles/HP Cloud Object Storage.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        try {
            s.getRequest(host, new DisabledLoginCallback());
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
        host.getCredentials().setUsername("tenant:key");
        assertEquals(URI.create("https://region-a.geo-1.identity.hpcloudsvc.com:35357/v2.0/tokens"), s.getRequest(host,
                new DisabledLoginCallback()).iterator().next().getURI());
        assertEquals(Client.AuthVersion.v20, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Authentication20AccessKeySecretKeyRequest.class,
                new ArrayList<AuthenticationRequest>(s.getRequest(host, new DisabledLoginCallback())).get(2).getClass());
    }

    @Test
    public void testProfileLondon() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("profiles/Rackspace UK.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        assertEquals("/v2.0/tokens", profile.getContext());
        assertEquals(URI.create("https://lon.identity.api.rackspacecloud.com/v2.0/tokens"), s.getRequest(host, new DisabledLoginCallback()).iterator().next().getURI());
        assertEquals(Client.AuthVersion.v20, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Authentication20RAXUsernameKeyRequest.class, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getClass());
    }

    @Test
    public void testDefault() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v1.0");
        final Host host = new Host(new SwiftProtocol(), "myidentityservice.example.net");
        assertEquals(URI.create("https://myidentityservice.example.net/v1.0"), s.getRequest(host, new DisabledLoginCallback()).iterator().next().getURI());
        assertEquals(Client.AuthVersion.v10, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals(Authentication10UsernameKeyRequest.class, s.getRequest(host, new DisabledLoginCallback()).iterator().next().getClass());
    }

    @Test
    public void testEmptyTenant() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        final Host host = new Host(new SwiftProtocol(), "auth.lts2.evault.com", new Credentials(
                "u", "p"
        ));
        s.getRequest(host, new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                credentials.setUsername("");
            }
        });
        assertEquals(":u", host.getCredentials().getUsername());
        s.getRequest(host, new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                fail();
            }
        });
    }
}
