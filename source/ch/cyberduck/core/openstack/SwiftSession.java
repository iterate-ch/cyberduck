package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.Region;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class SwiftSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private Map<String, Region> regions
            = new HashMap<String, Region>();

    private Map<Path, Distribution> distributions
            = new HashMap<Path, Distribution>();

    private PathContainerService containerService = new PathContainerService();

    public SwiftSession(Host h) {
        super(h);
    }

    @Override
    public Client connect(final HostKeyController key) throws BackgroundException {
        return new Client(super.connect());
    }

    protected Region getRegion(final Path container) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup region for container %s", container));
        }
        return this.getRegion(container.attributes().getRegion());
    }

    protected Region getRegion(final String location)
            throws ConnectionCanceledException {
        if(regions.containsKey(location)) {
            return regions.get(location);
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(regions.containsKey(null)) {
            final Region region = regions.get(null);
            log.info(String.format("Use default region %s", region));
            return region;
        }
        if(regions.isEmpty()) {
            throw new ConnectionCanceledException("No default region in authentication context");
        }
        final Region region = regions.values().iterator().next();
        log.warn(String.format("Fallback to first region found %s", region));
        return region;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            final AuthenticationResponse authentication = client.authenticate(
                    new SwiftAuthenticationService().getRequest(host, prompt));
            for(Region region : authentication.getRegions()) {
                regions.put(region.getRegionId(), region);
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        super.logout();
        regions.clear();
        distributions.clear();
    }

    @Override
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    /**
     * @return Publicy accessible URL of given object
     */
    @Override
    public String toHttpURL(final Path file) {
        if(distributions.containsKey(containerService.getContainer(file))) {
            return distributions.get(containerService.getContainer(file)).getURL(file);
        }
        return null;
    }

    @Override
    public Set<DescriptiveUrl> getURLs(final Path path) {
        // Storage URL is not accessible
        return this.getHttpURLs(path);
    }

    @Override
    public boolean exists(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                try {
                    return this.getClient().containerExists(this.getRegion(containerService.getContainer(file)),
                            file.getName());
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
                }
            }
            return super.exists(file);
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return new AttributedList<Path>(new SwiftContainerListService().list(this));
        }
        else {
            return new SwiftObjectListService(this).list(file, listener);
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(status.isResume()) {
                return this.getClient().getObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        status.getCurrent(), status.getLength());
            }
            return this.getClient().getObject(this.getRegion(containerService.getContainer(file)),
                    containerService.getContainer(file).getName(), containerService.getKey(file));
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        return this.getFeature(Write.class, new DisabledLoginController()).write(file, status);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Create container at top level
                this.getClient().createContainer(this.getRegion(region), file.getName());
            }
            else {
                // Create virtual directory. Use region of parent container.
                this.getClient().createPath(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                this.getClient().copyObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        containerService.getContainer(renamed).getName(), containerService.getKey(renamed));
                this.getClient().deleteObject(this.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : this.list(file, new DisabledListProgressListener())) {
                    this.rename(i, new Path(renamed, i.getName(), i.attributes().getType()));
                }
                try {
                    this.getClient().deleteObject(this.getRegion(containerService.getContainer(file)),
                            containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                catch(NotFoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Write.class) {
            return (T) new SwiftWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SwiftDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SwiftTouchFeature(this);
        }
        if(type == Location.class) {
            return (T) new Location() {
                @Override
                public Set<String> getLocations() {
                    return regions.keySet();
                }

                @Override
                public String getLocation(final Path container) throws BackgroundException {
                    return container.attributes().getRegion();
                }
            };
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        if(type == DistributionConfiguration.class) {
            for(Region region : regions.values()) {
                if(null != region.getCDNManagementUrl()) {
                    return (T) new SwiftDistributionConfiguration(this) {
                        @Override
                        public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
                            final Distribution distribution = super.read(container, method);
                            distributions.put(container, distribution);
                            return distribution;
                        }
                    };
                }
            }
            return null;
        }
        return super.getFeature(type, prompt);
    }
}