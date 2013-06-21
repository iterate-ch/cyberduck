package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesAuthenticationResponse;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudSession<FilesClient> {
    private static final Logger log = Logger.getLogger(CFSession.class);

    private FilesClient client;

    private Map<String, FilesRegion> regions
            = new HashMap<String, FilesRegion>();

    public CFSession(Host h) {
        super(h);
    }

    @Override
    public FilesClient connect() throws BackgroundException {
        client = new FilesClient(this.http());
        return client;
    }

    protected FilesRegion getRegion(final Path container) throws BackgroundException {
        final String location = container.attributes().getRegion();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup region for container %s in region %s", container, location));
        }
        if(regions.containsKey(location)) {
            return regions.get(location);
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(regions.containsKey(null)) {
            final FilesRegion region = regions.get(null);
            log.info(String.format("Use default region %s", region));
            return region;
        }
        if(regions.isEmpty()) {
            throw new ConnectionCanceledException("No default region in authentication context");
        }
        final FilesRegion region = regions.values().iterator().next();
        log.warn(String.format("Fallback to first region found %s", region));
        return region;
    }

    @Override
    public void login(final LoginController prompt) throws BackgroundException {
        try {
            final FilesAuthenticationResponse authentication = client.authenticate(new AuthenticationService().getRequest(host));
            for(FilesRegion region : authentication.getRegions()) {
                regions.put(region.getRegionId(), region);
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
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
        for(FilesRegion region : client.getRegions()) {
            if(null != region.getCDNManagementUrl()) {
                return true;
            }
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
    public DistributionConfiguration cdn(final LoginController prompt) {
        return new SwiftDistributionConfiguration(this);
    }

    @Override
    public IdentityConfiguration iam() {
        return new DefaultCredentialsIdentityConfiguration(host);
    }
}