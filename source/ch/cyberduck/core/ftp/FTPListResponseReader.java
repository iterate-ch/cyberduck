package ch.cyberduck.core.ftp;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Permission;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * @version $Id$
 */
public class FTPListResponseReader {
    private static final Logger log = Logger.getLogger(FTPListResponseReader.class);

    public AttributedList<Path> read(final FTPSession session, final Path parent,
                                     final FTPFileEntryParser parser, final List<String> replies) throws IOException, FTPInvalidListException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // At least one entry successfully parsed
        boolean success = false;
        for(String line : replies) {
            final FTPFile f = parser.parseFTPEntry(line);
            if(null == f) {
                continue;
            }
            final String name = f.getName();
            if(!success) {
                // Workaround for #2410. STAT only returns ls of directory itself
                // Workaround for #2434. STAT of symbolic link directory only lists the directory itself.
                if(parent.getAbsolute().equals(name)) {
                    log.warn(String.format("Skip %s", f.getName()));
                    continue;
                }
                if(name.contains(String.valueOf(Path.DELIMITER))) {
                    if(!name.startsWith(parent.getAbsolute() + Path.DELIMITER)) {
                        // Workaround for #2434.
                        log.warn(String.format("Skip listing entry with delimiter %s", name));
                        continue;
                    }
                }
            }
            success = true;
            if(name.equals(".") || name.equals("..")) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Skip %s", f.getName()));
                }
                continue;
            }
            final Path parsed = new Path(parent, PathNormalizer.name(name), f.getType() == FTPFile.DIRECTORY_TYPE ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
            switch(f.getType()) {
                case FTPFile.SYMBOLIC_LINK_TYPE:
                    parsed.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                    // Symbolic link target may be an absolute or relative path
                    if(f.getLink().startsWith(String.valueOf(Path.DELIMITER))) {
                        parsed.setSymlinkTarget(new Path(f.getLink(), parsed.attributes().getType()));
                    }
                    else {
                        parsed.setSymlinkTarget(new Path(parent, f.getLink(), parsed.attributes().getType()));
                    }
                    break;
            }
            if(parsed.attributes().isFile()) {
                parsed.attributes().setSize(f.getSize());
            }
            parsed.attributes().setOwner(f.getUser());
            parsed.attributes().setGroup(f.getGroup());
            Permission.Action u = Permission.Action.none;
            if(f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
                u = u.or(Permission.Action.read);
            }
            if(f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) {
                u = u.or(Permission.Action.write);
            }
            if(f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
                u = u.or(Permission.Action.execute);
            }
            Permission.Action g = Permission.Action.none;
            if(f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) {
                g = g.or(Permission.Action.read);
            }
            if(f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION)) {
                g = g.or(Permission.Action.write);
            }
            if(f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
                g = g.or(Permission.Action.execute);
            }
            Permission.Action o = Permission.Action.none;
            if(f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION)) {
                o = o.or(Permission.Action.read);
            }
            if(f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION)) {
                o = o.or(Permission.Action.write);
            }
            if(f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
                o = o.or(Permission.Action.execute);
            }
            parsed.attributes().setPermission(new Permission(u, g, o));
            final Calendar timestamp = f.getTimestamp();
            if(timestamp != null) {
                parsed.attributes().setModificationDate(timestamp.getTimeInMillis());
            }
            children.add(parsed);
        }
        if(!success) {
            throw new FTPInvalidListException(children);
        }
        return children;
    }
}
