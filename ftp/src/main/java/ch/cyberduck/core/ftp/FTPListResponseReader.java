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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.ftp.parser.FTPExtendedFile;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

/**
 * @version $Id$
 */
public class FTPListResponseReader implements FTPDataResponseReader {
    private static final Logger log = Logger.getLogger(FTPListResponseReader.class);

    private FTPFileEntryParser parser;

    private boolean lenient;

    public FTPListResponseReader(final FTPFileEntryParser parser) {
        this(parser, false);
    }

    public FTPListResponseReader(final FTPFileEntryParser parser, final boolean lenient) {
        this.parser = parser;
        this.lenient = lenient;
    }

    @Override
    public AttributedList<Path> read(final Path directory, final List<String> replies, final ListProgressListener listener)
            throws IOException, FTPInvalidListException, ConnectionCanceledException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // At least one entry successfully parsed
        boolean success = false;
        // Call hook for those implementors which need to perform some action upon the list after it has been created
        // from the server stream, but before any clients see the list
        parser.preParse(replies);
        for(String line : replies) {
            final FTPFile f = parser.parseFTPEntry(line);
            if(null == f) {
                continue;
            }
            final String name = f.getName();
            if(!success) {
                if(lenient) {
                    // Workaround for #2410. STAT only returns ls of directory itself
                    // Workaround for #2434. STAT of symbolic link directory only lists the directory itself.
                    if(directory.getName().equals(name)) {
                        log.warn(String.format("Skip %s matching parent directory name", f.getName()));
                        continue;
                    }
                    if(name.contains(String.valueOf(Path.DELIMITER))) {
                        if(!name.startsWith(directory.getAbsolute() + Path.DELIMITER)) {
                            // Workaround for #2434.
                            log.warn(String.format("Skip %s with delimiter in name", name));
                            continue;
                        }
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
            final Path parsed = new Path(directory, PathNormalizer.name(name), f.getType() == FTPFile.DIRECTORY_TYPE ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file));
            switch(f.getType()) {
                case FTPFile.SYMBOLIC_LINK_TYPE:
                    parsed.setType(EnumSet.of(Path.Type.file, Path.Type.symboliclink));
                    // Symbolic link target may be an absolute or relative path
                    final String target = f.getLink();
                    if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                        parsed.setSymlinkTarget(new Path(target, EnumSet.of(Path.Type.file)));
                    }
                    else {
                        parsed.setSymlinkTarget(new Path(String.format("%s/%s", directory.getAbsolute(), target),
                                EnumSet.of(Path.Type.file)));
                    }
                    break;
            }
            if(parsed.isFile()) {
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
            final Permission permission = new Permission(u, g, o);
            if(f instanceof FTPExtendedFile) {
                permission.setSetuid(((FTPExtendedFile) f).isSetuid());
                permission.setSetgid(((FTPExtendedFile) f).isSetgid());
                permission.setSticky(((FTPExtendedFile) f).isSticky());
            }
            parsed.attributes().setPermission(permission);
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
