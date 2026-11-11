package ch.cyberduck.core.dav;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.sardine.model.Multistatus;
import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SaxPropFindResponseHandlerTest {

    @Test
    public void testParseMultistatus() throws Exception {
        final String xml = "<?xml version=\"1.0\"?>" +
            "<multistatus xmlns=\"DAV:\"><response><href>/</href>" +
            "<propstat><prop><resourcetype><collection/></resourcetype></prop>" +
            "<status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";
        final Multistatus multistatus = new SaxPropFindResponseHandler().getMultistatus(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(multistatus);
        assertEquals(1, multistatus.getResponse().size());
    }

    /**
     * XXE: DOCTYPE with external parameter entity must be rejected and the DTD must never be fetched
     */
    @Test
    public void testRejectExternalDtd() throws Exception {
        final AtomicInteger requests = new AtomicInteger();
        final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            final byte[] dtd = "<!ENTITY % file SYSTEM \"file:///etc/hosts\">".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, dtd.length);
            exchange.getResponseBody().write(dtd);
            exchange.close();
        });
        server.start();
        try {
            final String xml = "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE multistatus [<!ENTITY % dtd SYSTEM \"http://127.0.0.1:" + server.getAddress().getPort() + "/exfil.dtd\"> %dtd;]>" +
                "<multistatus xmlns=\"DAV:\"><response><href>/</href>" +
                "<propstat><prop><resourcetype><collection/></resourcetype></prop>" +
                "<status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";
            try {
                new SaxPropFindResponseHandler().getMultistatus(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                fail("Expected failure for DOCTYPE declaration");
            }
            catch(IOException e) {
                // Expected
            }
            assertEquals(0, requests.get());
        }
        finally {
            server.stop(0);
        }
    }
}
