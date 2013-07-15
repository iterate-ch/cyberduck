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

import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

/**
 * @version $Id$
 */
public class SwiftContainerListService implements RootListService<SwiftSession> {
    private static final Logger log = Logger.getLogger(SwiftContainerListService.class);

    @Override
    public List<Path> list(final SwiftSession session) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List containers for %s", session));
        }
        try {
            final List<Path> containers = new ArrayList<Path>();
            final FilesClient client = session.getClient();
            for(FilesRegion region : client.getRegions()) {
                // List all containers
                for(FilesContainer f : client.listContainers(region)) {
                    final Path container = new Path(String.format("/%s", f.getName()),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    container.attributes().setRegion(f.getRegion().getRegionId());
                    if(Preferences.instance().getBoolean("cf.cdn.preload")) {
                        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class, new DisabledLoginController());
                        for(Distribution.Method method : cdn.getMethods(container)) {
                            cdn.read(container, method);
                        }
                    }
                    containers.add(container);
                }
            }
            return containers;
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Listing directory failed", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
