package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2AuthorizedUrlProviderTest {

    @Test
    public void testToUrl() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final B2FileidProvider fileid = new B2FileidProvider(session);
        new B2TouchFeature(session, fileid).touch(test, new TransferStatus());
        final B2AuthorizedUrlProvider provider = new B2AuthorizedUrlProvider(session, fileid);
        assertEquals(DescriptiveUrl.EMPTY, provider.toDownloadUrl(bucket, null, new DisabledPasswordCallback()));
        final DescriptiveUrl url = provider.toDownloadUrl(test, null, new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertNotNull(url.getUrl());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
