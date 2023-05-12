package ch.cyberduck.core.smb;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SMBTouchFeatureTest extends AbstractSMBTest {

    @Test
    public void testTouchLongFilename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path template = new Path(home, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path test = session.getFeature(Touch.class).touch(template, new TransferStatus())
                .withAttributes(session.getFeature(AttributesFinder.class).find(template));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(test.attributes(), session.getFeature(AttributesFinder.class).find(test));
    }

    @Test
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path template = new Path(home, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path test = session.getFeature(Touch.class).touch(template, new TransferStatus())
                .withAttributes(session.getFeature(AttributesFinder.class).find(template));
        assertTrue(new DefaultFindFeature(session).find(test));
    }

    @Test
    public void testTouchDeleteTouchLongFilename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path template = new Path(home, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file));
        final Path test = session.getFeature(Touch.class).touch(template, new TransferStatus())
                .withAttributes(session.getFeature(AttributesFinder.class).find(template));


        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final Path test2 = session.getFeature(Touch.class).touch(template, new TransferStatus())
                .withAttributes(session.getFeature(AttributesFinder.class).find(template));
        assertTrue(session.getFeature(Find.class).find(test2));
        assertEquals(test.attributes(), session.getFeature(AttributesFinder.class).find(test2));
    }
}
