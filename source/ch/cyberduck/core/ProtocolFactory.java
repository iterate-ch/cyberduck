package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public final class ProtocolFactory {
    private static final Logger log = Logger.getLogger(ProtocolFactory.class);

    public static final Protocol FTP = new FTPProtocol();
    public static final Protocol FTP_TLS = new FTPTLSProtocol();
    public static final Protocol SFTP = new SFTPProtocol();
    public static final Protocol S3_SSL = new S3Protocol();
    public static final Protocol WEBDAV = new DAVProtocol();
    public static final Protocol WEBDAV_SSL = new DAVSSLProtocol();
    public static final Protocol SWIFT = new SwiftProtocol();
    public static final Protocol GOOGLESTORAGE_SSL = new GoogleStorageProtocol();
    public static final Protocol AZURE = new AzureProtocol();

    /**
     * Ordered list of supported protocols.
     */
    private static final Set<Protocol> protocols
            = new LinkedHashSet<Protocol>();

    private ProtocolFactory() {
        //
    }

    public static void register() {
        // Order determines list in connection dropdown
        register(FTP);
        register(FTP_TLS);
        register(SFTP);
        register(WEBDAV);
        register(WEBDAV_SSL);
        register(SWIFT);
        register(S3_SSL);
        register(GOOGLESTORAGE_SSL);
        register(AZURE);
        // Order determines list in connection dropdown
        final Local bundled = LocalFactory.get(PreferencesFactory.get().getProperty("application.profiles.path"));
        if(bundled.exists()) {
            try {
                for(Local f : bundled.list().filter(new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
                    }
                })) {
                    final Profile profile = ProfileReaderFactory.get().read(f);
                    if(null == profile.getProtocol()) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding bundled protocol %s", profile));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    register(profile);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", bundled, e.getMessage()));
            }
        }
        // Load thirdparty protocols
        final Local library = LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"), "Profiles");
        if(library.exists()) {
            try {
                for(Local profile : library.list().filter(new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
                    }
                })) {
                    final Profile protocol = ProfileReaderFactory.get().read(profile);
                    if(null == protocol) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding thirdparty protocol %s", protocol));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    register(protocol);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", library, e.getMessage()));
            }
        }
    }

    public static void register(final Protocol p) {
        protocols.remove(p);
        protocols.add(p);
    }

    /**
     * @return List of protocols
     */
    public static List<Protocol> getEnabledProtocols() {
        final List<Protocol> enabled = new ArrayList<Protocol>();
        for(Protocol protocol : protocols) {
            if(protocol.isEnabled()) {
                enabled.add(protocol);
            }
        }
        return enabled;
    }

    /**
     * @param port Default port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(final int port) {
        for(Protocol protocol : getEnabledProtocols()) {
            if(protocol.getDefaultPort() == port) {
                return protocol;
            }
        }
        log.warn(String.format("Cannot find default protocol for port %d", port));
        return forName(PreferencesFactory.get().getProperty("connection.protocol.default"));
    }

    /**
     * @param identifier Provider name or hash code of protocol
     * @return Matching protocol or null if no match
     */
    public static Protocol forName(final String identifier) {
        for(Protocol protocol : protocols) {
            if(protocol.getProvider().equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : protocols) {
            if(String.valueOf(protocol.hashCode()).equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : protocols) {
            for(String scheme : protocol.getSchemes()) {
                if(scheme.equals(identifier)) {
                    return protocol;
                }
            }
        }
        log.warn(String.format("Unknown protocol with identifier %s", identifier));
        return null;
    }

    /**
     * @param scheme Protocol scheme
     * @return Standard protocol for this scheme. This is ambigous
     */
    public static Protocol forScheme(final String scheme) {
        for(Protocol protocol : getEnabledProtocols()) {
            for(int k = 0; k < protocol.getSchemes().length; k++) {
                if(protocol.getSchemes()[k].equals(scheme)) {
                    return protocol;
                }
            }
        }
        log.warn(String.format("Unknown scheme %s", scheme));
        return null;
    }

    /**
     * @param str Determine if URL can be handled by a registered protocol
     * @return True if known URL
     */
    public static boolean isURL(final String str) {
        if(StringUtils.isNotBlank(str)) {
            for(Protocol protocol : getEnabledProtocols()) {
                for(String scheme : protocol.getSchemes()) {
                    if(str.startsWith(scheme + "://")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}