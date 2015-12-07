package ch.cyberduck.core.serializer;

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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class HostDictionaryTest extends AbstractTestCase {

    @Test
    public void testDictionaryWorkdirRegion() {
        final Host h = new Host(new DAVProtocol(), "h", 66);
        final Path container = new Path("/container", EnumSet.of(Path.Type.directory));
        container.attributes().setRegion("r");
        h.setWorkdir(container);
        final Host deserialized = new HostDictionary(new DeserializerFactory(PlistDeserializer.class.getName())).deserialize(h.serialize(SerializerFactory.get()));
        assertEquals(h, deserialized);
        assertEquals("r", deserialized.getWorkdir().attributes().getRegion());
    }

    @Test
    public void testDeserialize() throws Exception {
        final Serializer dict = SerializerFactory.get();
        dict.setStringForKey("swift", "Protocol");
        dict.setStringForKey("unknown provider", "Provider");
        dict.setStringForKey("h", "Hostname");
        final Host host = new HostDictionary(new DeserializerFactory(PlistDeserializer.class.getName())).deserialize(dict.getSerialized());
        assertEquals(new SwiftProtocol(), host.getProtocol());
    }

}