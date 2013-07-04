package ch.cyberduck.core.cloud;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.versioning.VersioningConfiguration;

import java.util.Set;

/**
 * @version $Id: CloudSession.java 7011 2010-09-18 15:20:05Z dkocher $
 */
public abstract class CloudSession<C> extends HttpSession<C> {

    protected CloudSession(Host h) {
        super(h);
    }

    /**
     * Use ACL support.
     *
     * @return Always returning false because permissions should be set using ACLs
     */
    @Override
    public boolean isUnixPermissionsSupported() {
        return false;
    }

    @Override
    public boolean isWriteTimestampSupported() {
        return false;
    }

    public boolean isLoggingSupported() {
        return false;
    }

    /**
     * Creating files is only possible inside a bucket.
     *
     * @param workdir The working directory to create query
     * @return False if directory is root.
     */
    @Override
    public boolean isCreateFileSupported(final Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public boolean isAnalyticsSupported() {
        return this.isLoggingSupported();
    }

    public LoggingConfiguration getLogging(final Path container) throws BackgroundException {
        return new LoggingConfiguration(false);
    }

    public void setLogging(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        throw new BackgroundException("Not supported");
    }

    public LifecycleConfiguration getLifecycle(final Path container) throws BackgroundException {
        return new LifecycleConfiguration();
    }

    public void setLifecycle(final Path container, final LifecycleConfiguration configuration) throws BackgroundException {
        throw new BackgroundException("Not supported");
    }

    public boolean isVersioningSupported() {
        return false;
    }

    public boolean isLifecycleSupported() {
        return false;
    }

    public VersioningConfiguration getVersioning(final Path container) throws BackgroundException {
        return new VersioningConfiguration(false);
    }

    public void setVersioning(final Path container, final LoginController prompt,
                              final VersioningConfiguration configuration) throws BackgroundException {
        throw new BackgroundException("Not supported");
    }

    public boolean isLocationSupported() {
        return false;
    }

    /**
     * @param container Bucket
     * @return Bucket geographical location
     */
    public String getLocation(final Path container) throws BackgroundException {
        return container.attributes().getRegion();
    }

    @Override
    public Set<DescriptiveUrl> getURLs(final Path path) {
        // Storage URL is not accessible
        return this.getHttpURLs(path);
    }
}
