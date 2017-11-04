package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class MantaTouchFeatureTest extends AbstractMantaTest {

    @Test
    public void testTouch() throws Exception {
        final Path file = new Path(
                testPathPrefix,
                new AlphanumericRandomStringService().random(),
                EnumSet.of(Path.Type.file));
        new MantaTouchFeature(session).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(new MantaAttributesFinderFeature(session).find(file));
        new MantaDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespaceTouch() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path file = new Path(
                testPathPrefix,
                String.format("%s %s", randomStringService.random(), randomStringService.random()),
                EnumSet.of(Path.Type.file));
        new MantaTouchFeature(session).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(new MantaAttributesFinderFeature(session).find(file));
        new MantaDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
