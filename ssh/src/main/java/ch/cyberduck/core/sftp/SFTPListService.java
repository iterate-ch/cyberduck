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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.unicode.NFCNormalizer;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteDirectory;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPException;

public class SFTPListService implements ListService {
    private static final Logger log = Logger.getLogger(SFTPListService.class);

    private final NFCNormalizer normalizer = new NFCNormalizer();

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
            final RemoteDirectory handle = session.sftp().openDir(directory.getAbsolute());
            for(RemoteResourceInfo f : handle.scan(new RemoteResourceFilter() {
                @Override
                public boolean accept(RemoteResourceInfo remoteResourceInfo) {
                    return true;
                }
            })) {
                final PathAttributes attributes = feature.convert(f.getAttributes());
                final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                if(f.getAttributes().getType().equals(FileMode.Type.DIRECTORY)) {
                    type.add(Path.Type.directory);
                }
                if(f.getAttributes().getType().equals(FileMode.Type.REGULAR)) {
                    type.add(Path.Type.file);
                }
                if(f.getAttributes().getType().equals(FileMode.Type.SYMLINK)) {
                    type.add(Path.Type.symboliclink);
                }
                final Path file = new Path(directory, normalizer.normalize(f.getName()), type, attributes);
                if(this.post(file)) {
                    children.add(file);
                    listener.chunk(directory, children);
                }
            }
            handle.close();
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
            try {
                final String link = session.sftp().readLink(file.getAbsolute());
                if(link.startsWith(String.valueOf(Path.DELIMITER))) {
                    target = new Path(link, EnumSet.of(Path.Type.file));
                }
                else {
                    target = new Path(String.format("%s/%s", file.getParent().getAbsolute(), link),
                            EnumSet.of(Path.Type.file));
                }
                try {
                    if(session.sftp().stat(target.getAbsolute()).getType().equals(FileMode.Type.DIRECTORY)) {
                        type = Path.Type.directory;
                    }
                    else {
                        type = Path.Type.file;
                    }
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
                }
                file.setType(EnumSet.of(Path.Type.symboliclink, type));
                target.setType(EnumSet.of(type));
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