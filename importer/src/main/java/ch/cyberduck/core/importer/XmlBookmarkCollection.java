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

import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.xml.SecureXMLReaderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class XmlBookmarkCollection extends ThirdpartyBookmarkCollection {
    private static final Logger log = LogManager.getLogger(XmlBookmarkCollection.class);

    private static final long serialVersionUID = -3145592458663362423L;

    /**
     * Third-party files may declare a DOCTYPE such as Transmit 4 favorites referencing CoreData.dtd. Accept the
     * declaration but never load the external DTD subset or resolve external entities.
     */
    private static final Map<String, Boolean> FEATURES;

    static {
        final Map<String, Boolean> features = new LinkedHashMap<>();
        features.put(SecureXMLReaderFactory.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        features.put(SecureXMLReaderFactory.FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
        features.put(SecureXMLReaderFactory.FEATURE_LOAD_EXTERNAL_DTD, false);
        FEATURES = Collections.unmodifiableMap(features);
    }

    public XmlBookmarkCollection() {
        super();
    }

    public XmlBookmarkCollection(final HostPasswordStore keychain) {
        super(keychain);
    }

    protected abstract static class AbstractHandler extends DefaultHandler {
        private StringBuilder currentText = null;

        @Override
        public void startElement(final String uri, final String name, final String qName, final Attributes attrs) {
            this.currentText = new StringBuilder();
            this.startElement(name, attrs);
        }

        public abstract void startElement(String name, Attributes attrs);

        @Override
        public void endElement(final String uri, final String name, final String qName) {
            String elementText = currentText.toString();
            this.endElement(name, elementText);
        }

        public abstract void endElement(String name, String content);

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            currentText.append(ch, start, length);
        }
    }

    protected abstract AbstractHandler getHandler(final ProtocolFactory protocols);

    @Override
    protected void parse(final ProtocolFactory protocols, Local file) throws AccessDeniedException {
        this.read(protocols, file);
    }

    protected void read(final ProtocolFactory protocols, final Local child) throws AccessDeniedException {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(child.getInputStream(),
                    StandardCharsets.UTF_8));
            final AbstractHandler handler = this.getHandler(protocols);
            final XMLReader xr = SecureXMLReaderFactory.create(FEATURES);
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(in));
        }
        catch(SAXException | IOException e) {
            log.error("Error reading {}:{}", this.getFile(), e.getMessage());
        }
    }
}
