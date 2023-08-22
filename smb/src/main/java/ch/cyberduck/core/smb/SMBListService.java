package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;

public class SMBListService implements ListService {
    private static final Logger log = LogManager.getLogger(SMBListService.class);

    private final SMBSession session;
    private final Set<String> shares;

    public SMBListService(final SMBSession session) {
        this(session, Collections.singleton(session.getHost().getProtocol().getContext()));
    }

    public SMBListService(final SMBSession session, final Set<String> shares) {
        this.session = session;
        this.shares = shares;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> result = new AttributedList<>();
        if(directory.isRoot()) {
            for(String s : shares) {
                final Path share = new Path(s, EnumSet.of(Type.directory, Type.volume));
                try {
                    result.add(share.withAttributes(new SMBAttributesFinderFeature(session).find(share)));
                }
                catch(UnsupportedException e) {
                    if(log.isWarnEnabled()) {
                        log.warn(String.format("Skip unsupprted share %s", s));
                    }
                }
            }
        }
        else {
            try (final DiskShare share = session.openShare(directory)) {
                for(FileIdBothDirectoryInformation f : share.list(new SMBPathContainerService(session).getKey(directory))) {
                    final String filename = f.getFileName();
                    if(filename.equals(".") || filename.equals("..")) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Skip %s", f.getFileName()));
                        }
                        continue;
                    }
                    final EnumSet<Type> type = EnumSet.noneOf(Type.class);
                    long fileAttributes = f.getFileAttributes();
                    // check for all relevant file types and add them to the EnumSet
                    if((fileAttributes & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                        type.add(Type.directory);
                    }
                    else {
                        type.add(Type.file);
                    }
                    final PathAttributes attr = new PathAttributes();
                    attr.setAccessedDate(f.getLastAccessTime().toEpochMillis());
                    attr.setModificationDate(f.getLastWriteTime().toEpochMillis());
                    attr.setCreationDate(f.getCreationTime().toEpochMillis());
                    attr.setSize(f.getEndOfFile());
                    attr.setDisplayname(f.getFileName());
                    result.add(new Path(directory, filename, type, attr));
                }
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map("Listing directory {0} failed", e, directory);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read container configuration", e);
            }
        }
        return result;
    }
}
