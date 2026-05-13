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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ChainedComparisonServiceTest {

    @Test
    public void testCompare() {
        assertEquals(Comparison.equal, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L), new DefaultPathAttributes().setModificationDate(1000L)
        ));
        assertEquals(Comparison.equal, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L).setSize(1000L), new DefaultPathAttributes().setModificationDate(1000L).setSize(1000L)
        ));
        assertEquals(Comparison.remote, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L), new DefaultPathAttributes().setModificationDate(2000L)
        ));
        assertEquals(Comparison.remote, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L).setSize(1000L), new DefaultPathAttributes().setModificationDate(2000L).setSize(1000L)
        ));
        assertEquals(Comparison.unknown, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes(), new DefaultPathAttributes()
        ));
        assertEquals(Comparison.equal, new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L).setSize(1000L), new DefaultPathAttributes().setModificationDate(1000L).setSize(2000L)
        ));
        assertEquals(Comparison.notequal, new ChainedComparisonService(EnumSet.of(Comparison.unknown, Comparison.equal), new TimestampComparisonService(), new SizeComparisonService()).compare(
                Path.Type.file, new DefaultPathAttributes().setModificationDate(1000L).setSize(1000L), new DefaultPathAttributes().setModificationDate(1000L).setSize(2000L)
        ));
    }

    @Test
    public void testHashCode() {
        final PathAttributes attr = new DefaultPathAttributes().setModificationDate(1000L).setSize(2L);
        assertEquals(0, new ChainedComparisonService(new TimestampComparisonService()).hashCode(Path.Type.file, PathAttributes.EMPTY));
        assertNotEquals(0, new ChainedComparisonService(new TimestampComparisonService()).hashCode(Path.Type.file, attr));
        assertEquals(new ChainedComparisonService(new TimestampComparisonService()).hashCode(Path.Type.file, attr),
                new ChainedComparisonService(new TimestampComparisonService()).hashCode(Path.Type.file, attr));
        assertNotEquals(new TimestampComparisonService().hashCode(Path.Type.file, attr), new SizeComparisonService().hashCode(Path.Type.file, attr));
        assertNotEquals(new ChainedComparisonService(new TimestampComparisonService()).hashCode(Path.Type.file, attr),
                new ChainedComparisonService(new TimestampComparisonService(), new SizeComparisonService()).hashCode(Path.Type.file, attr));
    }
}