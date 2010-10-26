package ch.cyberduck.core.importer;

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

import ch.cyberduck.core.*;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * @version $Id$
 */
public class SmartFtpBookmarkCollection extends XmlBookmarkCollection {
    private static Logger log = Logger.getLogger(SmartFtpBookmarkCollection.class);

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
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.smartftp.location"));
    }

    @Override
    protected void parse(Local folder) {
        for(Local child : folder.children(new PathFilter<Local>() {
            public boolean accept(Local file) {
                if(file.attributes().isDirectory()) {
                    return true;
                }
                return "xml".equals(file.getExtension());
            }
        })) {
            if(child.attributes().isDirectory()) {
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
        public void startElement(String name) {
            if(name.equals("FavoriteItem")) {
                current = new Host(Preferences.instance().getProperty("connection.hostname.default"));
                current.getCredentials().setUsername(
                        Preferences.instance().getProperty("connection.login.anon.name"));
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
                            current.setProtocol(Protocol.FTP);
                            break;
                        case 2:
                        case 3:
                            current.setProtocol(Protocol.FTP_TLS);
                            break;
                        case 4:
                            current.setProtocol(Protocol.SFTP);
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
                    current.setTimestamp(DateParser.parse(elementText));
                }
                catch(InvalidDateException e) {
                    log.warn("Failed to parse timestamp:" + e.getMessage());
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
                            current.setFTPConnectMode(FTPConnectMode.ACTIVE);
                            break;
                        case 1:
                            current.setFTPConnectMode(FTPConnectMode.PASV);
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
                current.getCredentials().setIdentity(LocalFactory.createLocal(elementText));
            }
            else if(name.equals("FavoriteItem")) {
                add(current);
            }
        }
    }
}