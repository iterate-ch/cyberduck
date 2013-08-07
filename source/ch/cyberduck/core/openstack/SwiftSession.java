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
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
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

    private PathContainerService containerService
            = new PathContainerService();

    private SwiftDistributionConfiguration cdn
            = new SwiftDistributionConfiguration(this);

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
    }

    @Override
    public DescriptiveUrlBag getURLs(final Path file) {
        final DescriptiveUrlBag list = new SwiftUrlProvider(this).get(file);
        list.addAll(cdn.get(file));
        return list;
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
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Read.class) {
            return (T) new SwiftReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new SwiftWriteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new SwiftDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SwiftDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new SwiftMetadataFeature(this);
        }
        if(type == Copy.class) {
            return (T) new SwiftCopyFeature(this);
        }
        if(type == Move.class) {
            return (T) new SwiftMoveFeature(this);
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
            return (T) cdn;
        }
        return super.getFeature(type, prompt);
    }
}