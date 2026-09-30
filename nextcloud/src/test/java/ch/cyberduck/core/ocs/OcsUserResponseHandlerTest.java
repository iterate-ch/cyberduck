package ch.cyberduck.core.ocs;

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

import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OcsUserResponseHandlerTest {

    @Test
    public void testHandleEntityNextcloud() throws Exception {
        final String xml = "<?xml version=\"1.0\"?>\n" +
                "<ocs>\n" +
                " <meta>\n" +
                "  <status>ok</status>\n" +
                "  <statuscode>100</statuscode>\n" +
                "  <message>OK</message>\n" +
                "  <totalitems></totalitems>\n" +
                "  <itemsperpage></itemsperpage>\n" +
                " </meta>\n" +
                " <data>\n" +
                "  <enabled>1</enabled>\n" +
                "  <id>admin</id>\n" +
                "  <display-name>Administrator</display-name>\n" +
                "  <email>admin@example.com</email>\n" +
                "  <quota>\n" +
                "   <free>1000</free>\n" +
                "  </quota>\n" +
                " </data>\n" +
                "</ocs>";
        assertEquals("admin", new OcsUserResponseHandler().handleEntity(new StringEntity(xml, ContentType.APPLICATION_XML)));
    }

    @Test
    public void testHandleResponseOpenCloud() throws Exception {
        // User identifier from external identity provider differs from preferred_username claim
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ocs>\n" +
                " <meta>\n" +
                "  <status>ok</status>\n" +
                "  <statuscode>100</statuscode>\n" +
                "  <message>OK</message>\n" +
                " </meta>\n" +
                " <data>\n" +
                "  <id>00000000-1111-2222-3333-444444444444</id>\n" +
                "  <display-name>Jane Doe</display-name>\n" +
                "  <email>jane.doe@example.com</email>\n" +
                "  <user-type>Member</user-type>\n" +
                " </data>\n" +
                "</ocs>";
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
        response.setEntity(new StringEntity(xml, ContentType.TEXT_XML));
        assertEquals("00000000-1111-2222-3333-444444444444", new OcsUserResponseHandler().handleResponse(response));
    }

    @Test
    public void testIgnoreUnexpectedContentType() throws Exception {
        assertNull(new OcsUserResponseHandler().handleEntity(new StringEntity("<html></html>", ContentType.TEXT_HTML)));
    }
}
