package ch.cyberduck.core.cf;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;

import org.apache.commons.lang.StringUtils;

import java.net.URI;

import com.rackspacecloud.client.cloudfiles.method.Authentication10UsernameKeyRequest;
import com.rackspacecloud.client.cloudfiles.method.Authentication11UsernameKeyRequest;
import com.rackspacecloud.client.cloudfiles.method.Authentication20AccessKeySecretKeyRequest;
import com.rackspacecloud.client.cloudfiles.method.Authentication20RAXUsernameKeyRequest;
import com.rackspacecloud.client.cloudfiles.method.AuthenticationRequest;

/**
 * @version $Id$
 */
public class AuthenticationService {

    public AuthenticationRequest getRequest(final Host host) {
        final Credentials credentials = host.getCredentials();
        final StringBuilder url = new StringBuilder();
        url.append(host.getProtocol().getScheme().toString()).append("://");
        if(host.getProtocol().equals(Protocol.CLOUDFILES)) {
            url.append(Protocol.CLOUDFILES.getDefaultHostname());
            url.append(Protocol.CLOUDFILES.getContext());
            return new Authentication20RAXUsernameKeyRequest(
                    URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword(), null
            );
        }
        url.append(host.getHostname());
        url.append(":").append(host.getPort());
        if(StringUtils.isBlank(host.getProtocol().getContext())) {
            // Default to 1.0
            url.append(PathNormalizer.normalize(Preferences.instance().getProperty("cf.authentication.context")));
            return new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                    credentials.getUsername(), credentials.getPassword());
        }
        else {
            // Custom authentication context
            url.append(PathNormalizer.normalize(host.getProtocol().getContext()));
            if(host.getProtocol().getContext().contains("1.0")) {
                return new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                        credentials.getUsername(), credentials.getPassword());
            }
            else if(host.getProtocol().getContext().contains("1.1")) {
                return new Authentication11UsernameKeyRequest(URI.create(url.toString()),
                        credentials.getUsername(), credentials.getPassword());
            }
            else if(host.getProtocol().getContext().contains("2.0")) {
                // Prompt for tenant
                final String user;
                final String tenant;
                if(StringUtils.contains(credentials.getUsername(), ':')) {
                    user = StringUtils.split(credentials.getUsername(), ':')[1];
                    tenant = StringUtils.split(credentials.getUsername(), ':')[0];
                }
                else {
                    user = credentials.getUsername();
                    // Prompt for tenant
                    tenant = null;
                }
                return new Authentication20AccessKeySecretKeyRequest(
                        URI.create(url.toString()),
                        user, host.getCredentials().getPassword(), tenant
                );
            }
            else {
                // Default to 1.0
                return new Authentication10UsernameKeyRequest(URI.create(url.toString()),
                        credentials.getUsername(), credentials.getPassword());
            }
        }
    }
}

