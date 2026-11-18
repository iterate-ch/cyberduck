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

import java.io.File;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SecureXMLReaderFactoryTest {

    private static Map<String, Boolean> allowDoctype() {
        final Map<String, Boolean> features = new HashMap<>(SecureXMLReaderFactory.DEFAULT_FEATURES);
        features.remove(SecureXMLReaderFactory.FEATURE_DISALLOW_DOCTYPE_DECL);
        return features;
    }

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

    @Test
    public void testAllowDoctypeWithoutResolvingExternalResources() throws Exception {
        final AtomicInteger requests = new AtomicInteger();
        final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        final File secret = File.createTempFile("secret", ".txt");
        try {
            Files.write(secret.toPath(), "secret".getBytes(StandardCharsets.UTF_8));
            final String base = "http://127.0.0.1:" + server.getAddress().getPort();
            final String xml = "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE a SYSTEM \"" + base + "/external.dtd\" [" +
                "<!ENTITY % param SYSTEM \"" + base + "/param.dtd\"> %param;" +
                "<!ENTITY file SYSTEM \"" + secret.toURI() + "\">" +
                "<!ENTITY remote SYSTEM \"" + base + "/entity\">" +
                "]><a>&file;&remote;</a>";
            final StringBuilder content = new StringBuilder();
            final XMLReader reader = SecureXMLReaderFactory.create(allowDoctype());
            reader.setContentHandler(new DefaultHandler() {
                @Override
                public void characters(final char[] ch, final int start, final int length) {
                    content.append(ch, start, length);
                }
            });
            reader.parse(new InputSource(new StringReader(xml)));
            assertFalse(content.toString().contains("secret"));
            assertEquals(0, requests.get());
        }
        finally {
            server.stop(0);
            secret.delete();
        }
    }

    @Test
    public void testAllowDoctypeTransmitFavorites() throws Exception {
        final XMLReader reader = SecureXMLReaderFactory.create(allowDoctype());
        reader.setContentHandler(new DefaultHandler());
        reader.parse(new InputSource(new StringReader(
            "<?xml version=\"1.0\"?><!DOCTYPE database SYSTEM \"file:///System/Library/DTDs/CoreData.dtd\"><database/>")));
    }
}
