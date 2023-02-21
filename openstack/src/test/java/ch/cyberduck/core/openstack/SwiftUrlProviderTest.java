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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftUrlProviderTest extends AbstractSwiftTest {

    @Test
    public void testGet() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Map<Region, AccountInfo> accounts = new SwiftAccountLoader(session).operate(new DisabledPasswordCallback(),
                new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        assertEquals("https://storage101.iad3.clouddrive.com/v1/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee/test.cyberduck.ch/f",
                new SwiftUrlProvider(session, accounts).toUrl(new Path(container, "f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.provider).getUrl());
    }

    @Test
    public void testSigned() throws Exception {
        final Map<Region, AccountInfo> accounts = new SwiftAccountLoader(session).operate(new DisabledPasswordCallback(),
                new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)));
        final UrlProvider provider = new SwiftUrlProvider(session, accounts);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        container.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(file, new TransferStatus());
        final DescriptiveUrlBag list = provider.toUrl(file);
        final DescriptiveUrl signed = list.find(DescriptiveUrl.Type.signed);
        assertNotNull(signed);
        assertNotEquals(DescriptiveUrl.EMPTY, signed);
        assertFalse(list.filter(DescriptiveUrl.Type.signed).isEmpty());
        assertEquals(5, list.filter(DescriptiveUrl.Type.signed).size());
        for(DescriptiveUrl s : list.filter(DescriptiveUrl.Type.signed)) {
            assertNotNull(s);
            assertNotEquals(DescriptiveUrl.EMPTY, s);
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
