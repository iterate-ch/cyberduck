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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.SFTPExceptionMappingService;

import org.apache.log4j.Logger;

import java.io.IOException;

import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @version $Id$
 */
public class SFTPListService implements ListService {
    private static final Logger log = Logger.getLogger(SFTPListService.class);

    private SFTPSession session;

    public SFTPListService(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();

            for(SFTPv3DirectoryEntry f : session.sftp().ls(file.getAbsolute())) {
                if(f.filename.equals(".") || f.filename.equals("..")) {
                    continue;
                }
                SFTPv3FileAttributes attributes = f.attributes;
                final Path p = new Path(file,
                        f.filename, attributes.isDirectory() ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                if(null != attributes.size) {
                    if(p.attributes().isFile()) {
                        p.attributes().setSize(attributes.size);
                    }
                }
                String perm = attributes.getOctalPermissions();
                if(null != perm) {
                    try {
                        final String octal = Integer.toOctalString(attributes.permissions);
                        p.attributes().setPermission(new Permission(Integer.parseInt(octal.substring(octal.length() - 4))));
                    }
                    catch(IndexOutOfBoundsException e) {
                        log.warn(String.format("Failure parsing mode:%s", e.getMessage()));
                    }
                    catch(NumberFormatException e) {
                        log.warn(String.format("Failure parsing mode:%s", e.getMessage()));
                    }
                }
                if(null != attributes.uid) {
                    p.attributes().setOwner(attributes.uid.toString());
                }
                if(null != attributes.gid) {
                    p.attributes().setGroup(attributes.gid.toString());
                }
                if(null != attributes.mtime) {
                    p.attributes().setModificationDate(Long.parseLong(attributes.mtime.toString()) * 1000L);
                }
                if(null != attributes.atime) {
                    p.attributes().setAccessedDate(Long.parseLong(attributes.atime.toString()) * 1000L);
                }
                if(attributes.isSymlink()) {
                    final String target = session.sftp().readLink(p.getAbsolute());
                    final int type;
                    final Path symlink;
                    if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                        symlink = new Path(target, p.attributes().isFile() ? Path.FILE_TYPE : Path.DIRECTORY_TYPE);
                    }
                    else {
                        symlink = new Path(p.getParent(), target, p.attributes().isFile() ? Path.FILE_TYPE : Path.DIRECTORY_TYPE);
                    }
                    p.setSymlinkTarget(symlink);
                    final SFTPv3FileAttributes targetAttributes = session.sftp().stat(symlink.getAbsolute());
                    if(targetAttributes.isDirectory()) {
                        type = Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE;
                    }
                    else {
                        type = Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE;
                    }
                    p.attributes().setType(type);
                }
                children.add(p);
            }
            return children;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Listing directory failed", e, file);
        }
    }
}