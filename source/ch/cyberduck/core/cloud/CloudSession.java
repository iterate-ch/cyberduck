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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.http.HttpSession;

import java.util.Collections;
import java.util.List;

/**
 * @version $Id: CloudSession.java 7011 2010-09-18 15:20:05Z dkocher $
 */
public abstract class CloudSession extends HttpSession {

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
     * @param workdir The workdir to create query
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

    public boolean isLogging(final Path container) {
        throw new UnsupportedOperationException();
    }

    public void setLogging(final Path container, final boolean enabled, String destination) {
        throw new UnsupportedOperationException();
    }

    public String getLoggingTarget(final Path container) {
        throw new UnsupportedOperationException();
    }

    public Integer getTransition(final Path container) {
        throw new UnsupportedOperationException();
    }

    public Integer getExpiration(final Path container) {
        throw new UnsupportedOperationException();
    }

    public boolean isVersioningSupported() {
        return false;
    }

    public boolean isLifecycleSupported() {
        return false;
    }

    public boolean isVersioning(final Path container) {
        throw new UnsupportedOperationException();
    }

    public void setVersioning(final Path container, boolean mfa, boolean versioning) {
        throw new UnsupportedOperationException();
    }

    public boolean isLocationSupported() {
        return false;
    }

    /**
     * @param container Bucket
     * @return Bucket geographical location
     */
    public String getLocation(final Path container) {
        throw new UnsupportedOperationException();
    }

    public boolean isMultiFactorAuthentication(final Path container) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param container DNS container name
     * @return Generic hostname
     */
    public String getHostnameForContainer(final Path container) {
        return String.format("%s.%s", container, this.getHost().getHostname(true));
    }

    @Override
    public boolean isMetadataSupported() {
        return true;
    }

    /**
     * @return List of redundancy level options. Empty list
     *         no storage options are available.
     */
    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }

    /**
     * @return List of algorithms. Empty list
     *         no encryption options are available.
     */
    public List<String> getSupportedEncryptionAlgorithms() {
        return Collections.emptyList();
    }
}
