package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesAuthenticationResponse;
import com.rackspacecloud.client.cloudfiles.FilesAuthorizationException;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudSession {
    private static final Logger log = Logger.getLogger(CFSession.class);

    private FilesClient client;

    /**
     * Authentication key
     */
    private FilesAuthenticationResponse authentication;

    /**
     * Caching the root containers
     */
    private Map<Path, FilesRegion> containers = new HashMap<Path, FilesRegion>();

    public CFSession(Host h) {
        super(h);
    }

    @Override
    public FilesClient getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    @Override
    public void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();
        this.client = new FilesClient(this.http());
        this.login();
        this.fireConnectionDidOpenEvent();
    }


    protected FilesRegion getRegion(final Path container) throws IOException {
        if(containers.isEmpty()) {
            this.getContainers(true);
        }
        // Return default region
        return this.getClient().getRegions().iterator().next();
    }

    public List<Path> getContainers(final boolean reload) throws IOException {
        if(containers.isEmpty() || reload) {
            containers.clear();
            for(FilesContainer b : new ContainerListService().list(this)) {
                final Path container = PathFactory.createPath(this, String.valueOf(Path.DELIMITER), b.getName(),
                        Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                container.attributes().setRegion(b.getRegion().getRegionId());
                containers.put(container, b.getRegion());
            }
        }
        return new ArrayList<Path>(containers.keySet());
    }

    @Override
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        final FilesClient client = this.getClient();
        try {
            authentication = client.authenticate(new AuthenticationService().getRequest(host));
        }
        catch(FilesAuthorizationException e) {
            this.message(Locale.localizedString("Login failed", "Credentials"));
            controller.fail(host.getProtocol(), credentials);
            this.login();
        }
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                super.close();
            }
        }
        finally {
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    @Override
    public boolean isChecksumSupported() {
        return true;
    }

    @Override
    public boolean isCDNSupported() {
        try {
            for(FilesRegion region : this.getClient().getRegions()) {
                if(null != region.getCDNManagementUrl()) {
                    return true;
                }
            }
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isLocationSupported() {
        return new AuthenticationService().getRequest(this.getHost()).getVersion().equals(
                FilesClient.AuthVersion.v20
        );
    }

    @Override
    public String getLocation(final Path container) {
        return container.attributes().getRegion();
    }

    @Override
    public DistributionConfiguration cdn() {
        return new SwiftDistributionConfiguration(this);
    }

    @Override
    public IdentityConfiguration iam() {
        return new DefaultCredentialsIdentityConfiguration(host);
    }
}