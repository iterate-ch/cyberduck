package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultComparisonServiceTest {

    @Test
    public void compare() {
        final DefaultComparisonService c = new DefaultComparisonService(new TestProtocol());
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new PathAttributes().withETag("1"), new PathAttributes().withETag("1")));
        assertEquals(Comparison.unknown, c.compare(Path.Type.file, new PathAttributes().withETag("1"), new PathAttributes().withETag("2")));
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new PathAttributes().withETag("1").withSize(1L), new PathAttributes().withETag("2").withSize(1L)));
        assertEquals(Comparison.equal, c.compare(Path.Type.file, new PathAttributes().withETag("1").withSize(1L).withModificationDate(1L), new PathAttributes().withETag("2").withSize(1L).withModificationDate(1L)));
        assertEquals(Comparison.local, c.compare(Path.Type.file, new PathAttributes().withETag("1").withSize(1L).withModificationDate(2L), new PathAttributes().withETag("2").withSize(1L).withModificationDate(1L)));
    }
}