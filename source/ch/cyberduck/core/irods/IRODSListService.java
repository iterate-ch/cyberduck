package ch.cyberduck.core.irods;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;

import org.irods.jargon.core.exception.JargonException;

import java.io.File;
import java.util.EnumSet;

/**
 * @version $Id$
 */
public class IRODSListService implements ListService {

    private IRODSSession session;

    public IRODSListService(IRODSSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();

            File[] files = session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFile(directory.getAbsolute()).listFiles();
            for (File file : files) {
                final String normalizedPath = PathNormalizer.normalize(file.getAbsolutePath(), true);

                final PathAttributes attributes = new PathAttributes();
                attributes.setModificationDate(file.lastModified());
                attributes.setSize(file.length());

                children.add(new Path(directory, PathNormalizer.name(normalizedPath),
                        file.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file),
                        attributes));
                listener.chunk(directory, children);
            }
            return children;
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

}
