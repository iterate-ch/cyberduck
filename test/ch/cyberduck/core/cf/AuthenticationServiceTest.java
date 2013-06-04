package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.ProfileReaderFactory;

import org.junit.Test;

import java.net.URI;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.method.Authentication10UsernameKeyRequest;
import com.rackspacecloud.client.cloudfiles.method.Authentication20AccessKeySecretKeyRequest;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class AuthenticationServiceTest extends AbstractTestCase {

    @Test
    public void testGetRequest() throws Exception {
        AuthenticationService s = new AuthenticationService();
        assertEquals(FilesClient.AuthVersion.v20,
                s.getRequest(new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(), new Credentials("u", "P"))).getVersion());
        assertEquals(FilesClient.AuthVersion.v20,
                s.getRequest(new Host(Protocol.CLOUDFILES, "region-b.geo-1.identity.hpcloudsvc.com", new Credentials("u", "P"))).getVersion());
        assertEquals(FilesClient.AuthVersion.v20,
                s.getRequest(new Host(Protocol.CLOUDFILES, "myhost", new Credentials("u", "P"))).getVersion());
        assertEquals(FilesClient.AuthVersion.v10,
                s.getRequest(new Host(Protocol.SWIFT, "myhost", new Credentials("u", "P"))).getVersion());
        assertEquals("POST", s.getRequest(new Host(Protocol.CLOUDFILES, "myhost", new Credentials("u", "P"))).getMethod());
        assertEquals(URI.create("https://" + Protocol.CLOUDFILES.getDefaultHostname() + "/v2.0/tokens"),
                s.getRequest(new Host(Protocol.CLOUDFILES, "myhost", new Credentials("u", "P"))).getURI());
        assertEquals(URI.create("https://" + Protocol.CLOUDFILES.getDefaultHostname() + "/v2.0/tokens"),

                s.getRequest(new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(), new Credentials("u", "P"))).getURI());
        final Host host = new Host(Protocol.SWIFT, "identity.openstack.com", new Credentials("u", "P"));
        host.setPort(3451);
        assertEquals(URI.create("https://identity.openstack.com:3451/v1.0"), s.getRequest(host).getURI());
        assertEquals(FilesClient.AuthVersion.v10, s.getRequest(host).getVersion());
        assertEquals(Authentication10UsernameKeyRequest.class, s.getRequest(host).getClass());
    }

    @Test
    public void testProfile() throws Exception {
        AuthenticationService s = new AuthenticationService();
        final Profile profile = ProfileReaderFactory.get().read(LocalFactory.createLocal("profiles/HP Cloud Object Storage (US East).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        assertEquals(URI.create("https://region-b.geo-1.identity.hpcloudsvc.com:35357/v2.0/tokens"), s.getRequest(host).getURI());
        assertEquals(FilesClient.AuthVersion.v20, s.getRequest(host).getVersion());
        assertEquals(Authentication20AccessKeySecretKeyRequest.class, s.getRequest(host).getClass());
    }
}
