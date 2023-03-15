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
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteDirectory;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPException;

public class SFTPListService implements ListService {
    private static final Logger log = LogManager.getLogger(SFTPListService.class);

    private final SFTPSession session;
    private final SFTPAttributesFinderFeature attributes;

    public SFTPListService(final SFTPSession session) {
        this.session = session;
        this.attributes = new SFTPAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        try (RemoteDirectory handle = session.sftp().openDir(directory.getAbsolute())) {
            for(RemoteResourceInfo f : handle.scan(new RemoteResourceFilter() {
                @Override
                public boolean accept(RemoteResourceInfo remoteResourceInfo) {
                    return true;
                }
            })) {
                final PathAttributes attr = attributes.toAttributes(f.getAttributes());
                final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                switch(f.getAttributes().getType()) {
                    case DIRECTORY:
                        type.add(Path.Type.directory);
                        break;
                    case SYMLINK:
                        type.add(Path.Type.symboliclink);
                        break;
                    default:
                        type.add(Path.Type.file);
                        break;
                }
                final Path file = new Path(directory, f.getName(), type, attr);
                if(this.post(file)) {
                    children.add(file);
                    listener.chunk(directory, children);
                }
            }
            if(children.isEmpty()) {
                listener.chunk(directory, children);
            }
            return children;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    protected boolean post(final Path file) throws BackgroundException {
        if(file.isSymbolicLink()) {
            final Path target;
            Path.Type type;
            PathAttributes attr;
            try {
                final String link = session.sftp().readLink(file.getAbsolute());
                if(link.startsWith(String.valueOf(Path.DELIMITER))) {
                    target = new Path(PathNormalizer.normalize(link), EnumSet.of(Path.Type.file));
                }
                else {
                    target = new Path(PathNormalizer.normalize(String.format("%s/%s", file.getParent().getAbsolute(), link)), EnumSet.of(Path.Type.file));
                }
                try {
                    final FileAttributes stat = session.sftp().stat(target.getAbsolute());
                    if(stat.getType().equals(FileMode.Type.DIRECTORY)) {
                        type = Path.Type.directory;
                    }
                    else {
                        type = Path.Type.file;
                    }
                    attr = attributes.toAttributes(stat);
                }
                catch(SFTPException e) {
                    final BackgroundException reason = new SFTPExceptionMappingService().map(e);
                    if(reason instanceof NotfoundException) {
                        log.warn(String.format("Cannot find symbolic link target of %s. %s", file, reason.toString()));
                    }
                    else if(reason instanceof AccessDeniedException) {
                        log.warn(String.format("Cannot find symbolic link target of %s. %s", file, reason.toString()));
                    }
                    else if(reason instanceof InteroperabilityException) {
                        log.warn(String.format("Cannot find symbolic link target of %s. %s", file, reason.toString()));
                    }
                    else {
                        log.warn(String.format("Unknown failure reading symbolic link target of %s. %s", file, reason.toString()));
                        throw reason;
                    }
                    type = Path.Type.file;
                    attr = PathAttributes.EMPTY;
                }
                file.setType(EnumSet.of(Path.Type.symboliclink, type));
                target.setType(EnumSet.of(type));
                target.setAttributes(attr);
                file.setSymlinkTarget(target);
            }
            catch(IOException e) {
                log.warn(String.format("Failure to read symbolic link of %s. %s", file, e.getMessage()));
                return false;
            }
        }
        return true;
    }
}
