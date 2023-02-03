package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Home;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListFilteringFeatureTest {

    @Test
    public void testSearch() {
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))
        ));
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "F", EnumSet.of(Path.Type.file))
        ));
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v1"))
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v2"))
        ));
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.directory)).withAttributes(new PathAttributes().withVersionId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.directory)).withAttributes(new PathAttributes().withVersionId("v2"))
        ));
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withFileId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withFileId("v1"))
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withFileId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withFileId("v2"))
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.directory)).withAttributes(new PathAttributes().withFileId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.directory)).withAttributes(new PathAttributes().withFileId("v2"))
        ));
        assertTrue(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v1"))
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes().withVersionId("v1"))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes())
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file)).withAttributes(new PathAttributes() {
                    @Override
                    public boolean isDuplicate() {
                        return true;
                    }
                })
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.sensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "F", EnumSet.of(Path.Type.file))
        ));
        assertFalse(new ListFilteringFeature.ListFilteringPredicate(Protocol.Case.insensitive, new Path(Home.ROOT, "f", EnumSet.of(Path.Type.file))).test(
                new Path(Home.ROOT, "f2", EnumSet.of(Path.Type.file))
        ));
    }
}