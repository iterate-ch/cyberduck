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

import ch.cyberduck.core.serializer.ProtocolReaderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class ProtocolFactory {
    private static Logger log = Logger.getLogger(ProtocolFactory.class);

    /**
     *
     */
    public static void register() {
        // Order determines list in connection dropdown
        Protocol.FTP.register();
        Protocol.FTP_TLS.register();

        Protocol.SFTP.register();

        Protocol.WEBDAV.register();
        Protocol.WEBDAV_SSL.register();

        Protocol.IDISK.register();

        Protocol.S3_SSL.register();

        Protocol.GOOGLESTORAGE_SSL.register();
        Protocol.EUCALYPTUS.register();

        Protocol.CLOUDFILES.register();
        Protocol.SWIFT.register();

        Protocol.GDOCS_SSL.register();

        Protocol.AZURE_SSL.register();
        Protocol.DROPBOX_SSL.register();

        // Load thirdparty protocols
        final Local profiles = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"), "Profiles");
        if(profiles.exists()) {
            for(Local profile : profiles.children(new PathFilter<Local>() {
                public boolean accept(Local file) {
                    return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
                }
            })) {
                final Profile protocol = ProtocolReaderFactory.instance().read(profile);
                if(null == protocol) {
                    continue;
                }
                if(log.isInfoEnabled()) {
                    log.info(String.format("Adding thirdparty protocol %s", protocol));
                }
                protocol.register();
            }
        }
    }

    /**
     * @return List of enabled protocols
     */
    public static List<Protocol> getKnownProtocols() {
        return getKnownProtocols(true);
    }

    /**
     * @param filter Filter disabled protocols
     * @return List of protocols
     */
    public static List<Protocol> getKnownProtocols(boolean filter) {
        List<Protocol> list = new ArrayList<Protocol>(SessionFactory.getRegisteredProtocols());
        if(filter) {
            // Remove protocols not enabled
            for(Iterator<Protocol> iter = list.iterator(); iter.hasNext(); ) {
                final Protocol protocol = iter.next();
                if(!protocol.isEnabled()) {
                    iter.remove();
                }
            }
        }
        if(list.isEmpty()) {
            throw new RuntimeException("No protocols configured");
        }
        return list;
    }

    /**
     * @param port Default port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        for(Protocol protocol : getKnownProtocols(false)) {
            if(protocol.getDefaultPort() == port) {
                return protocol;
            }
        }
        log.warn("Cannot find default protocol for port:" + port);
        return forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param identifier Provider name or hash code of protocol
     * @return Matching protocol or default if no match
     */
    public static Protocol forName(final String identifier) {
        for(Protocol protocol : getKnownProtocols(false)) {
            if(protocol.getProvider().equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : getKnownProtocols(false)) {
            if(String.valueOf(protocol.hashCode()).equals(identifier)) {
                return protocol;
            }
        }
        log.fatal("Unknown protocol:" + identifier);
        return forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param scheme Protocol scheme
     * @return Standard protocol for this scheme. This is ambigous
     */
    public static Protocol forScheme(final String scheme) {
        for(Protocol protocol : getKnownProtocols(false)) {
            for(int k = 0; k < protocol.getSchemes().length; k++) {
                if(protocol.getSchemes()[k].equals(scheme)) {
                    return protocol;
                }
            }
        }
        log.fatal("Unknown scheme:" + scheme);
        return forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param str Determine if URL can be handleed by a registered protocol
     * @return True if known URL
     */
    public static boolean isURL(String str) {
        if(StringUtils.isNotBlank(str)) {
            for(Protocol protocol : getKnownProtocols(false)) {
                String[] schemes = protocol.getSchemes();
                for(String scheme : schemes) {
                    if(str.startsWith(scheme + "://")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
