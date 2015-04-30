package ch.cyberduck.fs;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DefaultPathReference;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public abstract class SessionFilesystem implements Filesystem {

    protected Session<?> session;

    protected PathCache cache;

    public SessionFilesystem(final Session<?> session, final PathCache cache) {
        this.session = session;
        this.cache = cache;
    }

    public void mount() throws BackgroundException {
        this.mount(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.directory, Path.Type.volume)));
    }

    @Override
    public Local getMountpoint() {
        final String volume = session.getHost().getHostname();
        Local target = LocalFactory.get(String.format("/Volumes/%s", volume));
        if(target.exists()) {
            // Make sure we do not mount to a already existing path
            final String parent = target.getParent().getAbsolute();
            int no = 0;
            while(target.exists()) {
                no++;
                String proposal = String.format("%s-%d", volume, no);
                target = LocalFactory.get(parent, proposal);
            }
        }
        return target;
    }

    @Override
    public void unmount() throws BackgroundException {
        session.close();
    }

    protected Path find(final Path workdir, final String input) throws NotfoundException {
        if(StringUtils.equals(String.valueOf(Path.DELIMITER), input)) {
            return workdir;
        }
        final String path = StringUtils.removeStart(input, String.valueOf(Path.DELIMITER));
        Path file = cache.lookup(new DefaultPathReference(new Path(workdir, path, EnumSet.of(Path.Type.file))));
        if(null == file) {
            file = cache.lookup(new DefaultPathReference(new Path(workdir, path, EnumSet.of(Path.Type.directory))));
        }
        if(null == file) {
            file = cache.lookup(new DefaultPathReference(new Path(workdir, path, EnumSet.of(Path.Type.directory, Path.Type.volume))));
        }
        if(null == file) {
            file = cache.lookup(new DefaultPathReference(new Path(workdir, path, EnumSet.of(Path.Type.file, Path.Type.symboliclink))));
        }
        if(null == file) {
            file = cache.lookup(new DefaultPathReference(new Path(workdir, path, EnumSet.of(Path.Type.directory, Path.Type.symboliclink))));
        }
        if(null == file) {
            throw new NotfoundException(path);
        }
        return file;
    }

}
