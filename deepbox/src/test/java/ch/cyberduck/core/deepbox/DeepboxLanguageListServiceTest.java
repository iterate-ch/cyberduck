package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DeepboxLanguageListServiceTest extends AbstractDeepboxTest {
    @Before
    public void setup() throws Exception {
        setup("deepbox.deepboxapp3.user", "de");
    }

    @Test
    public void testTrips() throws BackgroundException {
        final String name = "Dokumente";
        final String trips = "Reisen";
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path folder = new Path(String.format("/ORG 4 - DeepBox Desktop App/ORG3-Box1/%s", name), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        AttributedList<Path> list = new DeepboxListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertTrue(list.toStream().anyMatch(p -> p.getAbsolute().equals(String.format("/ORG 4 - DeepBox Desktop App/ORG3-Box1/%s/%s", name, trips))));
    }
}