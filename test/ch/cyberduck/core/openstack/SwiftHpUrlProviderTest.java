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

import ch.cyberduck.core.*;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
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
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path file = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SwiftTouchFeature(session).touch(file);
        final UrlProvider provider = new SwiftHpUrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                return properties.getProperty("hpcloud.secret");
            }
        });
        assertTrue(provider.toUrl(file).find(DescriptiveUrl.Type.signed).getUrl().startsWith(
                "https://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788/test.cyberduck.ch/" + file.getName()));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginController());
        session.close();
    }

}
