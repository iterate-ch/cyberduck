package ch.cyberduck.core.cloud;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @version $Id$
 */
public abstract class CloudPath extends Path {
    private static Logger log = Logger.getLogger(CloudPath.class);

    public <T> CloudPath(T dict) {
        super(dict);
    }

    protected CloudPath(String parent, String name, int type) {
        super(parent, name, type);
    }

    protected CloudPath(String path, int type) {
        super(path, type);
    }

    protected CloudPath(Path parent, final Local local) {
        super(parent, local);
    }

    @Override
    public Path getParent() {
        final Path parent = super.getParent();
        if(parent.isRoot()) {
            parent.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return parent;
    }

    /**
     * @return
     */
    public boolean isContainer() {
        return this.getParent().isRoot();
    }

    /**
     * @return The parent container/bucket of this file
     */
    public String getContainerName() {
        if(this.isRoot()) {
            return null;
        }
        CloudPath bucketname = this;
        while(!bucketname.isContainer()) {
            bucketname = (CloudPath) bucketname.getParent();
        }
        return bucketname.getName();
    }

    /**
     * @return Absolute path without the container name
     */
    public String getKey() {
        if(this.isContainer()) {
            return null;
        }
        if(this.getAbsolute().startsWith(Path.DELIMITER + this.getContainerName())) {
            return this.getAbsolute().substring(this.getContainerName().length() + 2);
        }
        return null;
    }

    /**
     * @return Modifiable HTTP header metatdata key and values
     */
    public abstract Map<String, String> readMetadata();

    /**
     * @param meta Modifiable HTTP header metatdata key and values
     * @return The updated headers of the object
     */
    public abstract void writeMetadata(Map<String, String> meta);
}
