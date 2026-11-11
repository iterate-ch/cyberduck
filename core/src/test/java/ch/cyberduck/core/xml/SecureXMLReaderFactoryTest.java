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

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SecureXMLReaderFactoryTest {

    @Test
    public void testParse() throws Exception {
        final XMLReader reader = SecureXMLReaderFactory.create();
        assertNotNull(reader);
        reader.setContentHandler(new DefaultHandler());
        reader.parse(new InputSource(new StringReader("<a xmlns=\"DAV:\"><b/></a>")));
    }

    @Test
    public void testRejectDoctype() throws Exception {
        final XMLReader reader = SecureXMLReaderFactory.create();
        reader.setContentHandler(new DefaultHandler());
        try {
            reader.parse(new InputSource(new StringReader(
                "<?xml version=\"1.0\"?><!DOCTYPE a [<!ENTITY % dtd SYSTEM \"http://127.0.0.1:1/x.dtd\"> %dtd;]><a/>")));
            fail("Expected failure for DOCTYPE declaration");
        }
        catch(SAXParseException e) {
            // Expected
        }
    }
}
