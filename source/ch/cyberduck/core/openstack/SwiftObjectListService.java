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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesObject;

/**
 * @version $Id$
 */
public class SwiftObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(SwiftObjectListService.class);

    private SwiftSession session;

    public SwiftObjectListService(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final int limit = Preferences.instance().getInteger("openstack.list.limit");
            String marker = null;
            List<FilesObject> list;
            do {
                final PathContainerService containerService = new PathContainerService();
                final Path container = containerService.getContainer(file);
                list = session.getClient().listObjectsStartingWith(session.getRegion(container), container.getName(),
                        containerService.isContainer(file) ? StringUtils.EMPTY : containerService.getKey(file) + Path.DELIMITER, null, limit, marker, Path.DELIMITER);
                for(FilesObject object : list) {
                    final Path child = new Path(file,
                            Path.getName(PathNormalizer.normalize(object.getName())),
                            "application/directory".equals(object.getMimeType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                    child.attributes().setOwner(child.attributes().getOwner());
                    child.attributes().setRegion(container.attributes().getRegion());
                    if(child.attributes().isFile()) {
                        child.attributes().setSize(object.getSize());
                        child.attributes().setChecksum(object.getMd5sum());
                        child.attributes().setETag(object.getMd5sum());
                        try {
                            final Date modified = DateParser.parse(object.getLastModified());
                            if(null != modified) {
                                child.attributes().setModificationDate(modified.getTime());
                            }
                        }
                        catch(InvalidDateException e) {
                            log.warn("Not ISO 8601 format:" + e.getMessage());
                        }
                    }
                    if(child.attributes().isDirectory()) {
                        child.attributes().setPlaceholder(true);
                        if(children.contains(child.getReference())) {
                            // There is already a placeholder object
                            continue;
                        }
                    }
                    children.add(child);
                    marker = object.getName();
                }
            }
            while(list.size() == limit);
            return children;

        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map("Listing directory failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
