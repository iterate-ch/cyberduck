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

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OcsCapabilitiesResponseHandlerTest {

    @Test
    public void testHandleEntitySpaces() throws Exception {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ocs>\n" +
                " <meta><status>ok</status><statuscode>100</statuscode><message>OK</message></meta>\n" +
                " <data>\n" +
                "  <capabilities>\n" +
                "   <core><pollinterval>60</pollinterval><webdav-root>remote.php/webdav</webdav-root></core>\n" +
                "   <files><versioning>true</versioning></files>\n" +
                "   <spaces><version>1.0.0</version><enabled>true</enabled><projects>true</projects><share_jail>true</share_jail></spaces>\n" +
                "  </capabilities>\n" +
                " </data>\n" +
                "</ocs>";
        final OcsCapabilities capabilities = new OcsCapabilitiesResponseHandler(new OcsCapabilities()).handleEntity(new StringEntity(xml, ContentType.TEXT_XML));
        assertTrue(capabilities.spaces);
        assertTrue(capabilities.projects);
        assertTrue(capabilities.sharejail);
    }

    @Test
    public void testHandleEntityNoSpaces() throws Exception {
        final String xml = "<?xml version=\"1.0\"?>\n" +
                "<ocs>\n" +
                " <meta><status>ok</status><statuscode>100</statuscode><message>OK</message></meta>\n" +
                " <data>\n" +
                "  <capabilities>\n" +
                "   <core><pollinterval>60</pollinterval><webdav-root>remote.php/webdav</webdav-root></core>\n" +
                "  </capabilities>\n" +
                " </data>\n" +
                "</ocs>";
        final OcsCapabilities capabilities = new OcsCapabilitiesResponseHandler(new OcsCapabilities()).handleEntity(new StringEntity(xml, ContentType.APPLICATION_XML));
        assertFalse(capabilities.spaces);
        assertFalse(capabilities.projects);
        assertFalse(capabilities.sharejail);
    }
}
