package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;

import org.junit.Test;

import java.net.URI;
import java.util.EnumSet;
import java.util.Iterator;

import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class SwiftHpUrlProviderTest extends AbstractTestCase {

    @Test
    public void testSigned() throws Exception {
        final SwiftProtocol protocol = new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        };
        final Host host = new Host(protocol, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, "a", EnumSet.of(Path.Type.file));
        final SwiftHpUrlProvider provider = new SwiftHpUrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                return properties.getProperty("hpcloud.secret");
            }
        });
        final Iterator<DescriptiveUrl> iterator = provider.sign(new Region("region-a.geo-1", URI.create("https://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788"), null),
                file, 1379500716).iterator();
        assertEquals("http://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788/test.cyberduck.ch/a?temp_url_sig=88650632417788:5C84TLCPJJ5FSSG6EDML:c4ff78486459b66d2ce45f8a3a51061e318f233a&temp_url_expires=1379500716",
                iterator.next().getUrl());
        assertEquals("https://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788/test.cyberduck.ch/a?temp_url_sig=88650632417788:5C84TLCPJJ5FSSG6EDML:c4ff78486459b66d2ce45f8a3a51061e318f233a&temp_url_expires=1379500716",
                iterator.next().getUrl());
    }
}
