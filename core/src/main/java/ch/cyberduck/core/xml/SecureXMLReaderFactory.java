package ch.cyberduck.core.xml;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Creates SAX readers safe to parse untrusted input. DOCTYPE declarations are rejected and
 * external entities and DTDs are never resolved to prevent XML external entity (XXE) injection.
 */
public final class SecureXMLReaderFactory {

    private SecureXMLReaderFactory() {
        //
    }

    /**
     * @return Namespace aware reader rejecting DOCTYPE declarations
     * @throws SAXException If the parser does not support the required security features
     */
    public static XMLReader create() throws SAXException {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return factory.newSAXParser().getXMLReader();
        }
        catch(ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }
}
