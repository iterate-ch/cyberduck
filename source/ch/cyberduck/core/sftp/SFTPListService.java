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
import java.util.EnumSet;

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

                final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                if(f.attributes.isDirectory()) {
                    type.add(Path.Type.directory);
                }
                if(f.attributes.isRegularFile()) {
                    type.add(Path.Type.file);
                }
                if(f.attributes.isSymlink()) {
                    type.add(Path.Type.symboliclink);
                }
                final Path file = new Path(directory, f.filename, type, attributes);
                if(file.isSymbolicLink()) {
                    try {
                        final String target = session.sftp().readLink(file.getAbsolute());
                        if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                            file.setSymlinkTarget(new Path(target, EnumSet.of(Path.Type.file)));
                        }
                        else {
                            file.setSymlinkTarget(new Path(String.format("%s/%s", directory.getAbsolute(), target),
                                    EnumSet.of(Path.Type.file)));
                        }
                        final Path symlinkTarget = file.getSymlinkTarget();
                        if(session.sftp().stat(symlinkTarget.getAbsolute()).isDirectory()) {
                            file.setType(EnumSet.of(Path.Type.symboliclink, Path.Type.directory));
                            file.getSymlinkTarget().setType(EnumSet.of(Path.Type.directory));
                        }
                        else {
                            file.setType(EnumSet.of(Path.Type.symboliclink, Path.Type.file));
                            file.getSymlinkTarget().setType(EnumSet.of(Path.Type.file));
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