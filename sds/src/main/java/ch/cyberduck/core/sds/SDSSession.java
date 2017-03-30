package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.AuthApi;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.ClientBuilder;
import java.util.EnumSet;

public class SDSSession extends HttpSession<ApiClient> {

    private String token;

    public SDSSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected ApiClient connect(final HostKeyCallback key) throws BackgroundException {
        final ApiClient client = new ApiClient();
        client.setBasePath(String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), host.getProtocol().getContext()));
        final ClientConfig configuration = new ClientConfig();
        configuration.property(ApacheClientProperties.CONNECTION_MANAGER, builder.createConnectionManager(builder.createRegistry()));
        configuration.property(ApacheClientProperties.REQUEST_CONFIG, builder.createRequestConfig(
                PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000
        ));
        configuration.connectorProvider(new ApacheConnectorProvider());
        client.setHttpClient(ClientBuilder.newClient(configuration));
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        try {
            // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
            final LoginResponse response = new AuthApi(client).login(new LoginRequest()
                    .authType(host.getProtocol().getAuthorization())
                    .language("en")
                    .login(host.getCredentials().getUsername())
                    .password(host.getCredentials().getPassword())
            );
            token = response.getToken();
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        client.getHttpClient().close();
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        if(directory.isRoot()) {
            try {
                final NodeList nodes = new NodesApi(client).getFsNodes(token, null, 0, 0L, null, null, null, null, null);
                for(Node node : nodes.getItems()) {
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setVersionId(String.valueOf(node.getId()));
                    attributes.setChecksum(Checksum.parse(node.getHash()));
                    attributes.setCreationDate(node.getCreatedAt().getTime());
                    attributes.setModificationDate(node.getUpdatedAt().getTime());
                    final EnumSet<AbstractPath.Type> type;
                    switch(node.getType()) {
                        case ROOM:
                            type = EnumSet.of(Path.Type.directory, Path.Type.volume);
                            break;
                        case FOLDER:
                            type = EnumSet.of(Path.Type.directory);
                            break;
                        default:
                            type = EnumSet.of(Path.Type.file);
                            break;
                    }
                    final Path file = new Path(directory, node.getName(), type, attributes);
                    children.add(file);
                    listener.chunk(directory, children);
                }
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Listing directory {0} failed", e, directory);
            }
        }
        else {

        }
        return children;
    }
}
