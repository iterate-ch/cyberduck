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
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftContainerListService implements RootListService {
    private static final Logger log = Logger.getLogger(SwiftContainerListService.class);

    private final ThreadFactory threadFactory
            = new NamedThreadFactory("cdn");

    private SwiftSession session;

    private Preferences preferences
            = Preferences.instance();

    private boolean cdn;

    private boolean size;

    private SwiftLocationFeature.SwiftRegion region;

    public SwiftContainerListService(final SwiftSession session) {
        this(session,
                Preferences.instance().getBoolean("openstack.cdn.preload"),
                Preferences.instance().getBoolean("openstack.container.size.preload"));
    }

    public SwiftContainerListService(SwiftSession session, SwiftLocationFeature.SwiftRegion region) {
        this(session,
                Preferences.instance().getBoolean("openstack.cdn.preload"),
                Preferences.instance().getBoolean("openstack.container.size.preload"));
        this.region = region;
    }

    public SwiftContainerListService(final SwiftSession session, final boolean cdn, final boolean size) {
        this.session = session;
        this.cdn = cdn;
        this.size = size;
    }

    @Override
    public List<Path> list(final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List containers for %s", session));
        }
        try {
            final List<Path> containers = new ArrayList<Path>();
            final int limit = preferences.getInteger("openstack.list.limit");
            final Client client = session.getClient();
            for(Region r : client.getRegions()) {
                if(region != null) {
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
                        final Path container = new Path(String.format("/%s", f.getName()),
                                EnumSet.of(Path.Type.volume, Path.Type.directory), attributes);
                        if(cdn) {
                            final DistributionConfiguration feature = session.getFeature(DistributionConfiguration.class);
                            if(feature != null) {
                                threadFactory.newThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for(Distribution.Method method : feature.getMethods(container)) {
                                            try {
                                                feature.read(container, method, new DisabledLoginController());
                                            }
                                            catch(BackgroundException e) {
                                                log.warn(String.format("Failure preloading CDN configuration for container %s %s", container, e.getMessage()));
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }
                        if(size) {
                            threadFactory.newThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final ContainerInfo info = client.getContainerInfo(f.getRegion(), f.getName());
                                        container.attributes().setSize(info.getTotalSize());
                                    }
                                    catch(IOException e) {
                                        log.warn(String.format("Failure reading info for container %s %s", container, e.getMessage()));
                                    }
                                }
                            }).start();
                        }
                        containers.add(container);
                        marker = f.getName();
                    }
                    listener.chunk(new AttributedList<Path>(containers));
                }
                while(!chunk.isEmpty());
            }
            return containers;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Listing directory failed", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
