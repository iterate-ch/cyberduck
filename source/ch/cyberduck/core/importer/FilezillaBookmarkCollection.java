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
import ch.cyberduck.core.ftp.FTPConnectMode;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class FilezillaBookmarkCollection extends XmlBookmarkCollection {
    private static Logger log = Logger.getLogger(FilezillaBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "de.filezilla";
    }

    @Override
    public String getName() {
        return "FileZilla";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.filezilla.location"));
    }

    @Override
    protected void parse(Local file) {
        this.read(file);
    }

    @Override
    protected AbstractHandler getHandler() {
        return new ServerHandler();
    }

    /**
     * Parser for Filezilla Site Manager.
     */
    private class ServerHandler extends AbstractHandler {
        private Host current = null;

        @Override
        public void startElement(String name) {
            if(name.equals("Server")) {
                current = new Host(Preferences.instance().getProperty("connection.hostname.default"));
            }
        }

        @Override
        public void endElement(String name, String elementText) {
            if(name.equals("Host")) {
                current.setHostname(elementText);
            }
            else if(name.equals("Protocol")) {
                try {
                    switch(Integer.parseInt(elementText)) {
                        case 0:
                            current.setProtocol(Protocol.FTP);
                            break;
                        case 3:
                        case 4:
                            current.setProtocol(Protocol.FTP_TLS);
                            break;
                        case 1:
                            current.setProtocol(Protocol.SFTP);
                            break;
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Unknown protocol:" + e.getMessage());
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
            else if(name.equals("MaximumMultipleConnections")) {
                if("0".equals(elementText)) {
                    current.setMaxConnections(null);
                }
                else {
                    try {
                        current.setMaxConnections(Integer.parseInt(elementText));
                    }
                    catch(NumberFormatException e) {
                        log.warn("Invalid MaximumMultipleConnections:" + e.getMessage());
                    }
                }
            }
            else if(name.equals("User")) {
                current.getCredentials().setUsername(elementText);
            }
            else if(name.equals("Logontype")) {
                try {
                    switch(Integer.parseInt(elementText)) {
                        case 0:
                            current.getCredentials().setUsername(Preferences.instance().getProperty("connection.login.anon.name"));
                            break;
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid Logontype:" + e.getMessage());
                }
            }
            else if(name.equals("Pass")) {
                current.getCredentials().setPassword(elementText);
            }
            else if(name.equals("Name")) {
                current.setNickname(elementText);
            }
            else if(name.equals("EncodingType")) {
                if("Auto".equals(elementText)) {
                    current.setEncoding(null);
                }
                else {
                    current.setEncoding(elementText);
                }
            }
            else if(name.equals("PasvMode")) {
                if("MODE_DEFAULT".equals(elementText)) {
                    current.setFTPConnectMode(null);
                }
                else if("MODE_PASSIVE".equals(elementText)) {
                    current.setFTPConnectMode(FTPConnectMode.PASV);
                }
                else if("MODE_ACTIVE".equals(elementText)) {
                    current.setFTPConnectMode(FTPConnectMode.PORT);
                }
            }
            else if(name.equals("Comments")) {
                current.setComment(elementText);
            }
            else if(name.equals("LocalDir")) {
                current.setDownloadFolder(elementText);
            }
            else if(name.equals("RemoteDir")) {
                if(StringUtils.isNotBlank(elementText)) {
                    StringBuilder b = new StringBuilder();
                    int i = 0;
                    //Path is written using wxString::Format(_T("%d %s "), (int)iter->Length(), iter->c_str());
                    for(String component : elementText.substring(3).split("\\s")) {
                        if(i % 2 == 0) {
                            b.append(component);
                        }
                        else {
                            b.append(Path.DELIMITER);
                        }
                        i++;
                    }
                    if(StringUtils.isNotBlank(b.toString())) {
                        current.setDefaultPath(b.toString());
                    }
                }
            }
            else if(name.equals("TimezoneOffset")) {
                ;
            }
            else if(name.equals("Server")) {
                add(current);
            }
        }
    }
}