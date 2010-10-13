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

import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @version $Id:$
 */
public abstract class XmlBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static Logger log = Logger.getLogger(XmlBookmarkCollection.class);

    protected static abstract class AbstractHandler extends DefaultHandler {
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

    protected abstract AbstractHandler getHandler();

    protected void read(Local child) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(child.getInputStream(),
                    Charset.forName("UTF-8")));
            AbstractHandler handler = this.getHandler();
            final XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(in));
        }
        catch(FileNotFoundException e) {
            log.error("Error reading " + this.getFile() + ":" + e.getMessage());
        }
        catch(SAXException e) {
            log.error("Error reading " + this.getFile() + ":" + e.getMessage());
        }
        catch(IOException e) {
            log.error("Error reading " + this.getFile() + ":" + e.getMessage());
        }
    }
}
