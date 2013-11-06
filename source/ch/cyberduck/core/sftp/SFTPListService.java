package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.log4j.Logger;

import java.io.IOException;

import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;

/**
 * @version $Id$
 */
public class SFTPListService implements ListService {
    private static final Logger log = Logger.getLogger(SFTPListService.class);

    private SFTPSession session;

    private SFTPAttributesFeature feature;

    public SFTPListService(final SFTPSession session) {
        this.session = session;
        this.feature = new SFTPAttributesFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            for(SFTPv3DirectoryEntry f : session.sftp().ls(directory.getAbsolute())) {
                if(f.filename.equals(".") || f.filename.equals("..")) {
                    continue;
                }
                final PathAttributes attributes = feature.convert(f.attributes);
                final Path file = new Path(directory, f.filename, attributes);
                if(attributes.isSymbolicLink()) {
                    try {
                        final String target = session.sftp().readLink(file.getAbsolute());
                        if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                            file.setSymlinkTarget(new Path(target, Path.FILE_TYPE));
                        }
                        else {
                            file.setSymlinkTarget(new Path(directory, target, Path.FILE_TYPE));
                        }
                        if(session.sftp().stat(file.getSymlinkTarget().getAbsolute()).isDirectory()) {
                            attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                        }
                        else {
                            attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                        }
                    }
                    catch(SFTPException e) {
                        final BackgroundException reason = new SFTPExceptionMappingService().map(e);
                        if(reason instanceof NotfoundException) {
                            log.warn(String.format("Cannot find symbolic link target of %s", file));
                        }
                        else if(reason instanceof AccessDeniedException) {
                            log.warn(String.format("Cannot read symbolic link target of %s", file));
                        }
                        else {
                            throw e;
                        }
                    }
                }
                children.add(file);
                listener.chunk(children);
            }
            return children;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Listing directory failed", e, directory);
        }
    }
}