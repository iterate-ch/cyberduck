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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

public class FilezillaBookmarkCollection extends XmlBookmarkCollection {
    private static final Logger log = Logger.getLogger(FilezillaBookmarkCollection.class);

    private static final long serialVersionUID = -4612895793983093594L;

    public FilezillaBookmarkCollection() {
        super();
    }

    public FilezillaBookmarkCollection(final PasswordStore keychain) {
        super(keychain);
    }

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
        return LocalFactory.get(PreferencesFactory.get().getProperty("bookmark.import.filezilla.location"));
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
        private Attributes attrs;

        @Override
        public void startElement(final String name, final Attributes attrs) {
            this.attrs = attrs;
            if(name.equals("Server")) {
                current = new Host(new FTPProtocol(), PreferencesFactory.get().getProperty("connection.hostname.default"));
            }
        }

        @Override
        public void endElement(final String name, final String elementText) {
            switch(name) {
                case "Host":
                    current.setHostname(elementText);
                    break;
                case "Protocol":
                    try {
                        switch(Integer.parseInt(elementText)) {
                            case 0:
                                current.setProtocol(new FTPProtocol());
                                break;
                            case 3:
                            case 4:
                                current.setProtocol(new FTPTLSProtocol());
                                break;
                            case 1:
                                current.setProtocol(new SFTPProtocol());
                                break;
                        }
                        // Reset port to default
                        current.setPort(-1);
                    }
                    catch(NumberFormatException e) {
                        log.warn("Unknown protocol:" + e.getMessage());
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
                case "User":
                    current.getCredentials().setUsername(elementText);
                    break;
                case "Logontype":
                    try {
                        switch(Integer.parseInt(elementText)) {
                            case 0:
                                current.getCredentials().setUsername(PreferencesFactory.get().getProperty("connection.login.anon.name"));
                                break;
                        }
                    }
                    catch(NumberFormatException e) {
                        log.warn("Invalid Logontype:" + e.getMessage());
                    }
                    break;
                case "Pass":
                    if(attrs.getIndex("encoding") == 0 && attrs.getValue(0).equals("base64")) {
                        if(Base64.isBase64(elementText)) {
                            current.getCredentials().setPassword(new String(Base64.decodeBase64(elementText)));
                        }
                    }
                    else {
                        current.getCredentials().setPassword(elementText);
                    }
                    break;
                case "Name":
                    current.setNickname(elementText);
                    break;
                case "EncodingType":
                    if("Auto".equals(elementText)) {
                        current.setEncoding(null);
                    }
                    else {
                        current.setEncoding(elementText);
                    }
                    break;
                case "PasvMode":
                    if("MODE_PASSIVE".equals(elementText)) {
                        current.setFTPConnectMode(FTPConnectMode.passive);
                    }
                    else if("MODE_ACTIVE".equals(elementText)) {
                        current.setFTPConnectMode(FTPConnectMode.active);
                    }
                    break;
                case "Comments":
                    current.setComment(elementText);
                    break;
                case "LocalDir":
                    current.setDownloadFolder(LocalFactory.get(elementText));
                    break;
                case "RemoteDir":
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
                    break;
                case "TimezoneOffset":
                    break;
                case "Server":
                    add(current);
                    break;
            }
        }
    }
}