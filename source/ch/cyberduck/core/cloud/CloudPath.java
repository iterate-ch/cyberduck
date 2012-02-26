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
import ch.cyberduck.core.http.HttpPath;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class CloudPath extends HttpPath {

    public <T> CloudPath(T dict) {
        super(dict);
        if(this.isContainer()) {
            this.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
    }

    protected CloudPath(String parent, String name, int type) {
        super(parent, name, type);
        if(this.isContainer()) {
            this.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
    }

    protected CloudPath(String path, int type) {
        super(path, type);
        if(this.isContainer()) {
            this.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
    }

    protected CloudPath(String parent, final Local local) {
        super(parent, local);
        if(this.isContainer()) {
            this.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
    }

    @Override
    public boolean isContainer() {
        return !StringUtils.contains(StringUtils.substring(this.getAbsolute(), 1), this.getPathDelimiter());
    }

    /**
     * @return The parent container/bucket of this file
     */
    @Override
    public String getContainerName() {
        if(this.isRoot()) {
            return null;
        }
        return this.getContainer().getName();
    }

    @Override
    public Path getContainer() {
        if(this.isRoot()) {
            return null;
        }
        CloudPath bucketname = this;
        while(!bucketname.isContainer()) {
            bucketname = (CloudPath) bucketname.getParent();
        }
        return bucketname;
    }

    /**
     * @return Absolute path without the container name
     */
    @Override
    public String getKey() {
        if(this.isContainer()) {
            return null;
        }
        if(this.getAbsolute().startsWith(String.valueOf(Path.DELIMITER) + this.getContainerName())) {
            return this.getAbsolute().substring(this.getContainerName().length() + 2);
        }
        return null;
    }

    @Override
    public Set<DescriptiveUrl> getURLs() {
        // Storage URL is not accessible
        return this.getHttpURLs();
    }
}