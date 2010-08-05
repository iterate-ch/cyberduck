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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.enterprisedt.net.ftp.FTPConnectMode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @version $Id$
 */
public class FilezillaBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(FilezillaBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "de.filezilla";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.filezilla.location"));
    }

    @Override
    protected void parse(Local file) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream(),
                    Charset.forName("UTF-8")));
            AbstractHandler handler = new ServerHandler();
            final XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(in));
        }
        catch(FileNotFoundException e) {
            log.error(e.getMessage());
        }
        catch(SAXException e) {
            log.error(e.getMessage());
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
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
                    log.error(e.getMessage());
                }
            }
            else if(name.equals("Port")) {
                try {
                    current.setPort(Integer.parseInt(elementText));
                }
                catch(NumberFormatException e) {
                    log.error(e.getMessage());
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
                        log.error(e.getMessage());
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
                    log.error(e.getMessage());
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
                    current.setFTPConnectMode(FTPConnectMode.ACTIVE);
                }
            }
            else if(name.equals("Comments")) {
                current.setComment(elementText);
            }
            else if(name.equals("RemoteDir")) {
                current.setDefaultPath(elementText);
            }
            else if(name.equals("TimezoneOffset")) {
                ;
            }
            else if(name.equals("Server")) {
                if(log.isDebugEnabled()) {
                    log.debug("Created new bookmark from listing: " + current);
                }
                add(current);
                if(current.getCredentials().validate(current.getProtocol())) {
                    // Save password in keychain instead of bookmark.
                    KeychainFactory.instance().addPassword(
                            current.getProtocol().getScheme(), current.getPort(),
                            current.getHostname(), current.getCredentials().getUsername(),
                            current.getCredentials().getPassword()
                    );
                }
                // Reset password
                current.getCredentials().setPassword(null);
            }
        }
    }

    private static abstract class AbstractHandler extends DefaultHandler {
        private StringBuffer currentText = null;

        @Override
        public void startElement(String uri, String name, String qName, Attributes attrs) {
            this.currentText = new StringBuffer();
            this.startElement(name);
        }

        public abstract void startElement(String name);

        @Override
        public void endElement(String uri, String name, String qName) {
            String elementText = this.currentText.toString();
            this.endElement(name, elementText);
        }

        public abstract void endElement(String name, String content);

        @Override
        public void characters(char ch[], int start, int length) {
            this.currentText.append(ch, start, length);
        }
    }
}