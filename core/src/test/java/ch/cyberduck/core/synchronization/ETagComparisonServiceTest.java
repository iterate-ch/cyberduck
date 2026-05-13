package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;

import junit.framework.TestCase;

public class ETagComparisonServiceTest extends TestCase {

    public void testCompare() {
        assertSame(Comparison.notequal, new ETagComparisonService().compare(Path.Type.file,
                new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("2")));
        assertSame(Comparison.equal, new ETagComparisonService().compare(Path.Type.file,
                new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("1")));
        assertSame(Comparison.equal, new ETagComparisonService().compare(Path.Type.file,
                new DefaultPathAttributes().setETag("1"), new DefaultPathAttributes().setETag("\"1\"")));
    }
}