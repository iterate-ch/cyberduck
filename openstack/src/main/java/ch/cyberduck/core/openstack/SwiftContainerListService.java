package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

public class SwiftContainerListService implements RootListService {
    private static final Logger log = Logger.getLogger(SwiftContainerListService.class);

    private final SwiftSession session;

    private final Preferences preferences
            = PreferencesFactory.get();

    private final boolean cdn;

    private final boolean size;

    private final SwiftRegionService regionService;
    private final SwiftLocationFeature.SwiftRegion region;

    public SwiftContainerListService(final SwiftSession session, final SwiftRegionService regionService, final SwiftLocationFeature.SwiftRegion region) {
        this(session, regionService, region,
                PreferencesFactory.get().getBoolean("openstack.cdn.preload"),
                PreferencesFactory.get().getBoolean("openstack.container.size.preload"));
    }

    public SwiftContainerListService(final SwiftSession session,
                                     final SwiftRegionService regionService,
                                     final SwiftLocationFeature.SwiftRegion region,
                                     final boolean cdn, final boolean size) {
        this.session = session;
        this.regionService = regionService;
        this.region = region;
        this.cdn = cdn;
        this.size = size;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List containers for %s", session));
        }
        try {
            final AttributedList<Path> containers = new AttributedList<Path>();
            final int limit = preferences.getInteger("openstack.list.container.limit");
            final Client client = session.getClient();
            for(final Region r : client.getRegions()) {
                if(region.getIdentifier() != null) {
                    if(!StringUtils.equals(r.getRegionId(), region.getIdentifier())) {
                        log.warn(String.format("Skip region %s", r));
                        continue;
                    }
                }
                // List all containers
                List<Container> chunk;
                String marker = null;
                do {
                    chunk = client.listContainers(r, limit, marker);
                    for(final Container f : chunk) {
                        final PathAttributes attributes = new PathAttributes();
                        attributes.setRegion(f.getRegion().getRegionId());
                        containers.add(new Path(String.format("/%s", f.getName()),
                                EnumSet.of(Path.Type.volume, Path.Type.directory), attributes));
                        marker = f.getName();
                    }
                    listener.chunk(directory, containers);
                }
                while(!chunk.isEmpty());
                if(cdn) {
                    final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
                    final ThreadPool<Void> pool = new DefaultThreadPool<Void>(2, "cdn");
                    try {
                        for(final Path container : containers) {
                            pool.execute(new Callable<Void>() {
                                @Override
                                public Void call() {
                                    for(Distribution.Method method : feature.getMethods(container)) {
                                        try {
                                            final Distribution distribution = feature.read(container, method, new DisabledLoginCallback());
                                            if(log.isInfoEnabled()) {
                                                log.info(String.format("Cached distribution %s", distribution));
                                            }
                                        }
                                        catch(BackgroundException e) {
                                            log.warn(String.format("Failure caching CDN configuration for container %s %s", container, e.getMessage()));
                                        }
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                    finally {
                        // Shutdown gracefully
                        pool.shutdown(true);
                    }
                }
                if(size) {
                    final ThreadPool<Long> pool = new DefaultThreadPool<Long>(2, "container");
                    try {
                        for(final Path container : containers) {
                            pool.execute(new Callable<Long>() {
                                @Override
                                public Long call() throws IOException {
                                    final long size = client.getContainerInfo(r, container.getName()).getTotalSize();
                                    container.attributes().setSize(size);
                                    return size;
                                }
                            });
                        }
                    }
                    finally {
                        // Shutdown gracefully
                        pool.shutdown(true);
                    }
                }
            }
            return containers;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
