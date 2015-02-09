package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.azure.AzureSession;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.gstorage.GoogleStorageSession;
import ch.cyberduck.core.openstack.SwiftSession;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public final class SessionFactory {
    private static final Logger log = Logger.getLogger(SessionFactory.class);

    private SessionFactory() {
        //
    }

    public static Session create(final Host host) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create session for %s", host));
        }
        final Protocol protocol = host.getProtocol();
        switch(protocol.getType()) {
            case ftp:
                return new FTPSession(host);
            case ssh:
                return new SFTPSession(host);
            case s3:
                return new S3Session(host);
            case googlestorage:
                return new GoogleStorageSession(host);
            case swift:
                return new SwiftSession(host);
            case dav:
                return new DAVSession(host);
            case azure:
                return new AzureSession(host);
            default:
                throw new FactoryException(protocol.getType().name());
        }
    }

    public static Session create(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create session for %s", host));
        }
        final Protocol protocol = host.getProtocol();
        switch(protocol.getType()) {
            case ftp:
                return new FTPSession(host, trust, key);
            case ssh:
                return new SFTPSession(host);
            case s3:
                return new S3Session(host, trust, key);
            case googlestorage:
                return new GoogleStorageSession(host, trust, key);
            case swift:
                return new SwiftSession(host, trust, key);
            case dav:
                return new DAVSession(host, trust, key);
            case azure:
                return new AzureSession(host, trust, key);
            default:
                throw new FactoryException(protocol.getType().name());
        }
    }
}
