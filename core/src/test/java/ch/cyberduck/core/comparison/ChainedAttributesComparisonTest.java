package ch.cyberduck.core.comparison;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.synchronization.Comparison;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class ChainedAttributesComparisonTest {

    @Test
    public void compare() {
        assertEquals(Comparison.equal, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L), new PathAttributes().withModificationDate(1L)
        ));
        assertEquals(Comparison.equal, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L).withSize(1L), new PathAttributes().withModificationDate(1L).withSize(1L)
        ));
        assertEquals(Comparison.remote, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L), new PathAttributes().withModificationDate(2L)
        ));
        assertEquals(Comparison.remote, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L).withSize(1L), new PathAttributes().withModificationDate(2L).withSize(1L)
        ));
        assertEquals(Comparison.unknown, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes(), new PathAttributes()
        ));
        assertEquals(Comparison.equal, new ChainedAttributesComparison(new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L).withSize(1L), new PathAttributes().withModificationDate(1L).withSize(2L)
        ));
        assertEquals(Comparison.notequal, new ChainedAttributesComparison(EnumSet.of(Comparison.unknown, Comparison.equal), new TimestampAttributesComparison(), new SizeAttributesComparison()).compare(
                AbstractPath.Type.file, new PathAttributes().withModificationDate(1L).withSize(1L), new PathAttributes().withModificationDate(1L).withSize(2L)
        ));
    }
}