package ch.cyberduck.core.dav.microsoft;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.RFC1123DateFormatter;

import org.joda.time.DateTime;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.sardine.DavResource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MicrosoftIISDAVAttributesFinderFeatureTest {

    @Test
    public void testCustomModified_PropertyAvailable() throws Exception {
        final MicrosoftIISDAVAttributesFinderFeature f = new MicrosoftIISDAVAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        final String ts = "Mon, 29 Oct 2018 21:14:06 GMT";
        map.put(MicrosoftIISDAVTimestampFeature.LAST_MODIFIED_WIN32_CUSTOM_NAMESPACE, ts);
        when(mock.getModified()).thenReturn(new DateTime("2018-11-01T15:31:57Z").toDate());
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(new RFC1123DateFormatter().parse(ts).getTime(), attrs.getModificationDate());
    }

    @Test
    public void testCustomModified_PropertyNotAvailable() throws Exception {
        final MicrosoftIISDAVAttributesFinderFeature f = new MicrosoftIISDAVAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        final Date modified = new DateTime("2018-11-01T15:31:57Z").toDate();
        when(mock.getModified()).thenReturn(modified);
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(modified.getTime(), attrs.getModificationDate());
    }
}
