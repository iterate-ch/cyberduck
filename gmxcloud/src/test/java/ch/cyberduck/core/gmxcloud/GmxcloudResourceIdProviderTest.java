package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudResourceIdProviderTest extends AbstractGmxcloudTest{

    @Test
    public void getFileId() throws Exception {
        assertEquals("ROOT", new GmxcloudResourceIdProvider(session).getFileId(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
    }

    @Test
    public void testParseResourceUri() throws Exception {
        assertEquals("1030364733607248477", GmxcloudResourceIdProvider.getResourceIdFromResourceUri("../../resource/1030364733607248477"));
        assertEquals("1030365219475424239", GmxcloudResourceIdProvider.getResourceIdFromResourceUri("1030365219475424239"));
    }
}