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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    protected CloudPath(String parent, final Local local) {
        super(parent, local);
    }

    @Override
    public abstract CloudSession getSession();

    @Override
    public Path getParent() {
        final CloudPath parent = (CloudPath) super.getParent();
        if(parent.isRoot()) {
            parent.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        if(parent.isContainer()) {
            parent.attributes().setType(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return parent;
    }

    /**
     * @return
     */
    public boolean isContainer() {
        return super.getParent().isRoot();
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
    public String getKey() {
        if(this.isContainer()) {
            return null;
        }
        if(this.getAbsolute().startsWith(String.valueOf(Path.DELIMITER) + this.getContainerName())) {
            return this.getAbsolute().substring(this.getContainerName().length() + 2);
        }
        return null;
    }

    /**
     * @return Modifiable HTTP header metatdata key and values
     */
    public abstract void readMetadata();

    /**
     * @param meta Modifiable HTTP header metatdata key and values
     * @return The updated headers of the object
     */
    public abstract void writeMetadata(Map<String, String> meta);

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readUnixPermission() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract AttributedList<Path> list();

    public DescriptiveUrl toSignedUrl() {
        return new DescriptiveUrl(null, null);
    }

    /**
     * Including URLs to CDN.
     *
     * @return
     */
    @Override
    public List<DescriptiveUrl> getHttpURLs() {
        List<DescriptiveUrl> urls = new ArrayList<DescriptiveUrl>(Arrays.asList(
                new DescriptiveUrl(this.toURL(), MessageFormat.format(Locale.localizedString("{0} URL"),
                        this.getHost().getProtocol().getScheme().toUpperCase())))
        );
        CloudSession session = this.getSession();
        for(Distribution.Method method : session.getSupportedDistributionMethods()) {
            Distribution distribution = session.getDistribution(this.getContainerName(), method);
            if(null != distribution) {
                // Cached
                urls.add(new DescriptiveUrl(distribution.getCnameUrl(this.getKey()),
                        MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString(method.toString(), "S3")))
                );
            }
            // Not cached yet.
        }
        return urls;
    }
}
