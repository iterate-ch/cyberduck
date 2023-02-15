package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrickTouchFeatureTest extends AbstractBrickTest {

    @Test
    public void testCaseSensitivity() throws Exception {
        final Path container = new BrickDirectoryFeature(session).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = StringUtils.lowerCase(new AlphanumericRandomStringService().random());
        final Path lowerCase = new BrickTouchFeature(session)
                .touch(new Path(container, filename, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final Path upperCase = new BrickTouchFeature(session)
                .touch(new Path(container, StringUtils.capitalize(filename), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new BrickFindFeature(session).find(lowerCase));
        assertTrue(new BrickFindFeature(session).find(upperCase));
        new BrickDeleteFeature(session).delete(Collections.singletonList(lowerCase), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}