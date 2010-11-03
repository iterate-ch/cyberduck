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

/**
 * @version $Id: FilezillaBookmarkCollection.java 7465 2010-11-02 16:28:09Z dkocher $
 */
public class CrossFtpBookmarkCollection extends XmlBookmarkCollection {
    private static Logger log = Logger.getLogger(CrossFtpBookmarkCollection.class);

    @Override
    public String getBundleIdentifier() {
        return "com.crossftp";
    }

    @Override
    public String getName() {
        return "CrossFTP";
    }

    @Override
    public Local getFile() {
        return LocalFactory.createLocal(Preferences.instance().getProperty("bookmark.import.crossftp.location"));
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
        public void startElement(String uri, String name, String qName, Attributes attrs) {
            if(name.equals("site")) {
                current = new Host(attrs.getValue("hName"));
                current.setNickname(attrs.getValue("name"));
                current.getCredentials().setUsername(attrs.getValue("un"));
                current.setWebURL(attrs.getValue("wURL"));
                current.setComment(attrs.getValue("comm"));
                current.setDefaultPath(attrs.getValue("path"));
                String protocol = attrs.getValue("ftpPType");
                try {
                    switch(Integer.valueOf(protocol)) {
                        case 1:
                            current.setProtocol(Protocol.FTP);
                            break;
                        case 2:
                        case 3:
                        case 4:
                            current.setProtocol(Protocol.FTP_TLS);
                            break;
                        case 6:
                            current.setProtocol(Protocol.WEBDAV);
                            break;
                        case 7:
                            current.setProtocol(Protocol.WEBDAV_SSL);
                            break;
                        case 8:
                        case 9:
                            current.setProtocol(Protocol.S3_SSL);
                            break;
                    }
                }
                catch(NumberFormatException e) {
                    log.warn("Unknown protocol:" + e.getMessage());
                }
                try {
                    current.setPort(Integer.parseInt(attrs.getValue("port")));
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid Port:" + e.getMessage());
                }
            }
            super.startElement(uri, name, qName, attrs);
        }

        @Override
        public void startElement(String name) {
            ;
        }

        @Override
        public void endElement(String name, String elementText) {
            if(name.equals("site")) {
                add(current);
            }
        }
    }
}