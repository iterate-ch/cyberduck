package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

public class Transmit4BookmarkCollection extends XmlBookmarkCollection {
    private static final Logger log = Logger.getLogger(Transmit4BookmarkCollection.class);

    @Override
    protected AbstractHandler getHandler() {
        return new FavoriteHandler();
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.transmit4.location"));
    }

    @Override
    public String getBundleIdentifier() {
        return "com.panic.Transmit";
    }

    private class FavoriteHandler extends AbstractHandler {
        private Host current = null;

        // Current attribute name
        private String attribute;

        @Override
        public void startElement(final String name, final Attributes attrs) {
            switch(name) {
                case "object":
                    final String type = attrs.getValue("type");
                    switch(type) {
                        case "FAVORITE":
                            current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
                            break;
                        default:
                            log.warn(String.format("Unsupported type: %s", type));
                            break;
                    }
                    break;
                case "attribute":
                    attribute = attrs.getValue("name");
                    break;
            }
        }

        @Override
        public void endElement(final String name, final String elementText) {
            if(null == current) {
                return;
            }
            switch(name) {
                case "object":
                    add(current);
                    current = null;
                    break;
                case "attribute":
                    switch(attribute) {
                        case "username":
                            current.getCredentials().setUsername(elementText);
                            break;
                        case "server":
                            current.setHostname(elementText);
                            break;
                        case "protocol":
                            switch(StringUtils.lowerCase(elementText)) {
                                case "webdav":
                                    current.setProtocol(new DAVProtocol());
                                    break;
                                case "webdavs":
                                    current.setProtocol(new DAVSSLProtocol());
                                    break;
                                case "sftp":
                                    current.setProtocol(new SFTPProtocol());
                                    break;
                                case "ftptls":
                                case "ftpssl":
                                    current.setProtocol(new FTPTLSProtocol());
                                    break;
                                case "ftp":
                                    current.setProtocol(new FTPProtocol());
                                    break;
                                case "s3":
                                    current.setProtocol(new S3Protocol());
                                    break;
                            }
                            // Reset port to default
                            current.setPort(-1);
                            break;
                        case "port":
                            final Integer value = Integer.valueOf(elementText);
                            if(value > 0) {
                                current.setPort(value);
                            }
                            break;
                        case "nickname":
                            current.setNickname(elementText);
                            break;
                        case "initialremotepath":
                            current.setDefaultPath(elementText);
                            break;
                    }
            }
        }
    }
}
