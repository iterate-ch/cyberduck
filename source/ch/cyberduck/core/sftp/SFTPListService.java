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
                if(f.getFilename().equals(".") || f.getFilename().equals("..")) {
                    continue;
                }
                final PathAttributes attributes = feature.convert(f.getAttributes());
                final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                if(f.getAttributes().isDirectory()) {
                    type.add(Path.Type.directory);
                }
                if(f.getAttributes().isRegularFile()) {
                    type.add(Path.Type.file);
                }
                if(f.getAttributes().isSymlink()) {
                    type.add(Path.Type.symboliclink);
                }
                final Path file = new Path(directory, f.getFilename(), type, attributes);
                this.post(file);
                children.add(file);
                listener.chunk(children);
            }
            return children;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Listing directory failed", e, directory);
        }
    }

    protected void post(final Path file) throws BackgroundException {
        if(file.isSymbolicLink()) {
            try {
                final String link = session.sftp().readLink(file.getAbsolute());
                final Path target;
                final Path.Type type;
                if(link.startsWith(String.valueOf(Path.DELIMITER))) {
                    target = new Path(link, EnumSet.of(Path.Type.file));
                }
                else {
                    target = new Path(String.format("%s/%s", file.getParent().getAbsolute(), link),
                            EnumSet.of(Path.Type.file));
                }
                if(session.sftp().stat(target.getAbsolute()).isDirectory()) {
                    type = Path.Type.directory;
                }
                else {
                    type = Path.Type.file;
                }
                file.setType(EnumSet.of(Path.Type.symboliclink, type));
                target.setType(EnumSet.of(type));
                file.setSymlinkTarget(target);
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
                    throw reason;
                }
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map("Cannot read file attributes", e, file);
            }
        }
    }
}