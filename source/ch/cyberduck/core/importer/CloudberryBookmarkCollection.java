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
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

/**
 * @version $Id$
 */
public abstract class CloudberryBookmarkCollection extends XmlBookmarkCollection {
    private static final Logger log = Logger.getLogger(CloudberryBookmarkCollection.class);

    private static final long serialVersionUID = 2245328157886337606L;

    @Override
    protected void parse(Local file) throws AccessDeniedException {
        this.read(file);
    }

    @Override
    protected AbstractHandler getHandler() {
        return new ServerHandler();
    }

    private class ServerHandler extends AbstractHandler {
        private Host current = null;

        @Override
        public void startElement(String uri, String name, String qName, Attributes attrs) {
            if(name.equals("Settings")) {
                String type = attrs.getValue("xsi:type");
                if("GoogleSettings".equals(type)) {
                    current = new Host(ProtocolFactory.GOOGLESTORAGE_SSL, ProtocolFactory.GOOGLESTORAGE_SSL.getDefaultHostname(), ProtocolFactory.GOOGLESTORAGE_SSL.getDefaultPort());
                }
                else if("S3Settings".equals(type)) {
                    current = new Host(ProtocolFactory.S3_SSL, ProtocolFactory.S3_SSL.getDefaultHostname(), ProtocolFactory.S3_SSL.getDefaultPort());
                }
                else if("DunkelSettings".equals(type)) {
                    current = new Host(ProtocolFactory.S3_SSL, ProtocolFactory.S3_SSL.getDefaultHostname(), ProtocolFactory.S3_SSL.getDefaultPort());
                }
                else {
                    log.warn("Unsupported connection type:" + type);
                }
            }
            super.startElement(uri, name, qName, attrs);
        }

        @Override
        public void startElement(String name, Attributes attrs) {
            //
        }

        @Override
        public void endElement(String name, String elementText) {
            if(null == current) {
                return;
            }
            if(name.equals("ServicePoint")) {
                current.setHostname(elementText);
            }
            else if(name.equals("AWSKey")) {
                current.getCredentials().setUsername(elementText);
            }
            else if(name.equals("Account")) {
                current.getCredentials().setUsername(elementText);
            }
            else if(name.equals("SharedKey")) {
                current.getCredentials().setPassword(elementText);
            }
            else if(name.equals("Name")) {
                current.setNickname(elementText);
            }
            else if(name.equals("Settings")) {
                add(current);
                current = null;
            }
        }
    }
}
