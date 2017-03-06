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

import java.util.regex.Pattern;

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

            @Override
            public Pattern toPattern() {
                return Pattern.compile(".*\\.xml");
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
            if(log.isDebugEnabled()) {
                log.debug(String.format("endElement:%s,%s", name, elementText));
            }
            switch(name) {
                case "Host":
                    current.setHostname(elementText);
                    break;
                case "Protocol":
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
                        // Reset port to default
                        current.setPort(-1);
                    }
                    catch(NumberFormatException e) {
                        log.warn("Unknown Protocol:" + e.getMessage());
                    }
                    break;
                case "Port":
                    try {
                        current.setPort(Integer.parseInt(elementText));
                    }
                    catch(NumberFormatException e) {
                        log.warn("Invalid Port:" + e.getMessage());
                    }
                    break;
                case "LastConnect":
                    try {
                        current.setTimestamp(new ISO8601DateParser().parse(elementText));
                    }
                    catch(InvalidDateException e) {
                        log.warn(String.format("Failed to parse timestamp from %s %s", elementText, e.getMessage()));
                    }
                    break;
                case "User":
                    current.getCredentials().setUsername(elementText);
                    break;
                case "Name":
                    current.setNickname(elementText);
                    break;
                case "DataConnectionMethod":
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
                    break;
                case "Description":
                    current.setComment(elementText);
                    break;
                case "Path":
                    current.setDefaultPath(elementText);
                    break;
                case "HTTP":
                    current.setWebURL(elementText);
                    break;
                case "WebRootPath":
                    current.setDefaultPath(elementText);
                    break;
                case "PrivateKey":
                    current.getCredentials().setIdentity(LocalFactory.get(elementText));
                    break;
                case "FavoriteItem":
                    add(current);
                    break;
            }
        }
    }
}