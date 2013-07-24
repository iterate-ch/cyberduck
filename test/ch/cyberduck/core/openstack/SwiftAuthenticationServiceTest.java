package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileReaderFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import java.net.URI;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.method.Authentication10UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20AccessKeySecretKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20RAXUsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class SwiftAuthenticationServiceTest extends AbstractTestCase {

    @Test
    public void testGetRequest() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com", new Credentials("u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(Protocol.SWIFT, "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals(Client.AuthVersion.v10,
                s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals("GET", s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("u", "P")),
                new DisabledLoginController()).getMethod());
        assertEquals("POST", s.getRequest(new Host(Protocol.SWIFT, "lon.identity.api.rackspacecloud.com", new Credentials("u", "P")),
                new DisabledLoginController()).getMethod());
        final Host host = new Host(Protocol.SWIFT, "identity.openstack.com", new Credentials("u", "P"));
        host.setPort(3451);
        assertEquals(URI.create("https://identity.openstack.com:3451/v1.0"), s.getRequest(host, new DisabledLoginController()).getURI());
        assertEquals(Client.AuthVersion.v10, s.getRequest(host, new DisabledLoginController()).getVersion());
        assertEquals(Authentication10UsernameKeyRequest.class, s.getRequest(host, new DisabledLoginController()).getClass());
    }

    @Test
    public void testGetDefault2() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(Protocol.SWIFT, "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("tenant:u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("tenant:u", "P")),
                        new DisabledLoginController()).getVersion());
        assertEquals(Authentication20UsernamePasswordRequest.class,
                s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("tenant:u", "P")),
                        new DisabledLoginController()).getClass());
    }

    @Test(expected = LoginCanceledException.class)
    public void testGetDefault2NoTenant() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService("/v2.0/tokens");
        assertEquals(Client.AuthVersion.v20,
                s.getRequest(new Host(Protocol.SWIFT, "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("u", "P")),
                        new DisabledLoginController()).getVersion());
    }

    @Test
    public void testProfileHPCloud() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/HP Cloud Object Storage.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        try {
            s.getRequest(host, new DisabledLoginController());
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
        host.getCredentials().setUsername("tenant:key");
        assertEquals(URI.create("https://region-b.geo-1.identity.hpcloudsvc.com:35357/v2.0/tokens"), s.getRequest(host,
                new DisabledLoginController()).getURI());
        assertEquals(Client.AuthVersion.v20, s.getRequest(host, new DisabledLoginController()).getVersion());
        assertEquals(Authentication20AccessKeySecretKeyRequest.class, s.getRequest(host, new DisabledLoginController()).getClass());
    }

    @Test
    public void testProfileLondon() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/Rackspace UK.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        assertEquals("/v2.0/tokens", profile.getContext());
        assertEquals(URI.create("https://lon.identity.api.rackspacecloud.com/v2.0/tokens"), s.getRequest(host, new DisabledLoginController()).getURI());
        assertEquals(Client.AuthVersion.v20, s.getRequest(host, new DisabledLoginController()).getVersion());
        assertEquals(Authentication20RAXUsernameKeyRequest.class, s.getRequest(host, new DisabledLoginController()).getClass());
    }

    @Test
    public void testDefault() throws Exception {
        final SwiftAuthenticationService s = new SwiftAuthenticationService();
        final Host host = new Host(Protocol.SWIFT, "myidentityservice.example.net");
        assertEquals(URI.create("https://myidentityservice.example.net/v1.0"), s.getRequest(host, new DisabledLoginController()).getURI());
        assertEquals(Client.AuthVersion.v10, s.getRequest(host, new DisabledLoginController()).getVersion());
        assertEquals(Authentication10UsernameKeyRequest.class, s.getRequest(host, new DisabledLoginController()).getClass());
    }
}
