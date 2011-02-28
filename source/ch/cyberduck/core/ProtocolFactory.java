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

import java.util.List;

/**
 * @version $Id:$
 */
public class ProtocolFactory {
    private static Logger log = Logger.getLogger(ProtocolFactory.class);

    /**
     *
     */
    public static void register() {
        Protocol.FTP.register();
        Protocol.FTP_TLS.register();
        Protocol.SFTP.register();
        Protocol.WEBDAV.register();
        Protocol.WEBDAV_SSL.register();
        Protocol.IDISK.register();
        Protocol.S3_SSL.register();
        Protocol.S3.register();
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
                if(log.isInfoEnabled()) {
                    log.info("Adding thirdparty protocol:" + protocol);
                }
                protocol.register();
            }
        }
    }

    public static List<Protocol> getKnownProtocols() {
        List<Protocol> list = SessionFactory.getRegisteredProtocols();
        if(list.isEmpty()) {
            throw new RuntimeException("No protocols configured");
        }
        return list;
    }

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        for(Protocol protocol : getKnownProtocols()) {
            if(protocol.getDefaultPort() == port) {
                return protocol;
            }
        }
        log.warn("Cannot find default protocol for port:" + port);
        return forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param identifier
     * @return
     */
    public static Protocol forName(final String identifier) {
        for(Protocol protocol : getKnownProtocols()) {
            if(protocol.getIdentifier().equals(identifier)) {
                return protocol;
            }
        }
        log.fatal("Unknown protocol:" + identifier);
        return forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param scheme
     * @return
     */
    public static Protocol forScheme(final String scheme) {
        for(Protocol protocol : getKnownProtocols()) {
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
     * @param str
     * @return
     */
    public static boolean isURL(String str) {
        if(StringUtils.isNotBlank(str)) {
            for(Protocol protocol : getKnownProtocols()) {
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
