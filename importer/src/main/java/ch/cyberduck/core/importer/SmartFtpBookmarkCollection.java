package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.date.ISO8601DateParser;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

public class SmartFtpBookmarkCollection extends XmlBookmarkCollection {
    private static final Logger log = Logger.getLogger(SmartFtpBookmarkCollection.class);

    private static final long serialVersionUID = 6455585501577444740L;

    @Override
    public String getBundleIdentifier() {
        return "com.smartftp";
    }

    @Override
    public String getName() {
        return "SmartFTP";
    }

    @Override
    public Local getFile() {
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.smartftp.location"));
    }

    @Override
    protected void parse(final Local folder) throws AccessDeniedException {
        for(Local child : folder.list().filter(new Filter<Local>() {
            @Override
            public boolean accept(Local file) {
                if(file.isDirectory()) {
                    return true;
                }
                return "xml".equals(file.getExtension());
            }
        })) {
            if(child.isDirectory()) {
                this.parse(child);
            }
            else {
                this.read(child);
            }
        }
    }

    @Override
    protected AbstractHandler getHandler() {
        return new ServerHandler();
    }

    /**
     * Parser for SmartFTP favorites.
     */
    private class ServerHandler extends AbstractHandler {
        private Host current = null;

        @Override
        public void startElement(String name, Attributes attrs) {
            if(name.equals("FavoriteItem")) {
                current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
                current.getCredentials().setUsername(
                        PreferencesFactory.get().getProperty("connection.login.anon.name"));
            }
        }

        @Override
        public void endElement(String name, String elementText) {
            log.debug("endElement:" + name + "," + elementText);
            if(name.equals("Host")) {
                current.setHostname(elementText);
            }
            else if(name.equals("Protocol")) {
                try {
                    switch(Integer.parseInt(elementText)) {
                        case 1:
                            current.setProtocol(new FTPProtocol());
                            break;
                        case 2:
                        case 3:
                            current.setProtocol(new FTPTLSProtocol());
                            break;
                        case 4:
                            current.setProtocol(new SFTPProtocol());
                            break;
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Unknown Protocol:" + e.getMessage());
                }
            }
            else if(name.equals("Port")) {
                try {
                    current.setPort(Integer.parseInt(elementText));
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid Port:" + e.getMessage());
                }
            }
            else if(name.equals("LastConnect")) {
                try {
                    current.setTimestamp(new ISO8601DateParser().parse(elementText));
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("Failed to parse timestamp from %s %s", elementText, e.getMessage()));
                }
            }
            else if(name.equals("User")) {
                current.getCredentials().setUsername(elementText);
            }
            else if(name.equals("Name")) {
                current.setNickname(elementText);
            }
            else if(name.equals("DataConnectionMethod")) {
                try {
                    switch(Integer.parseInt(elementText)) {
                        case 0:
                            current.setFTPConnectMode(FTPConnectMode.active);
                            break;
                        case 1:
                            current.setFTPConnectMode(FTPConnectMode.passive);
                            break;
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid connect mode:" + e.getMessage());
                }
            }
            else if(name.equals("Description")) {
                current.setComment(elementText);
            }
            else if(name.equals("Path")) {
                current.setDefaultPath(elementText);
            }
            else if(name.equals("HTTP")) {
                current.setWebURL(elementText);
            }
            else if(name.equals("WebRootPath")) {
                current.setDefaultPath(elementText);
            }
            else if(name.equals("PrivateKey")) {
                current.getCredentials().setIdentity(LocalFactory.get(elementText));
            }
            else if(name.equals("FavoriteItem")) {
                add(current);
            }
        }
    }
}