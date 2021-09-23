package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static ch.cyberduck.core.AbstractPath.Type.file;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OfflineGraphCopyMoveFeatureTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection features() {
        return Arrays.asList(Move.class, Copy.class);
    }

    @Parameterized.Parameter(0)
    public Class feature;

    @Test
    public void testSharepoint() {
        final SharepointSession session = new SharepointSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Object feature = session.getFeature(this.feature);
        final List<TestCase> cases = new ArrayList<>();

        // Rename Tests
        // Assumption: Cyberduck always uses same source and target path to indicate rename.
        cases.add(new TestCase("/Default", directory, false));
        cases.add(new TestCase("/Default/Drives/Drive", directory, false));
        cases.add(new TestCase("/Default/Drives/Drive/Folder/Test", file, true));
        cases.add(new TestCase("/Default/Drives/Drive/Test", file, true));
        cases.add(new TestCase("/Default/Sites", directory, false));
        cases.add(new TestCase("/Default/Sites/Site", directory, false));
        cases.add(new TestCase("/Default/Sites/Site/Drives", directory, false));
        cases.add(new TestCase("/Default/Sites/Site/Sites", directory, false));
        cases.add(new TestCase("/Groups", directory, false));
        cases.add(new TestCase("/Groups/Group", directory, false));
        cases.add(new TestCase("/Groups/Group/Drive", directory, false));
        cases.add(new TestCase("/Groups/Group/Drive/Folder/Test", file, true));
        cases.add(new TestCase("/Groups/Group/Drive/Test", file, true));
        cases.add(new TestCase("/Invalid", directory, false));
        cases.add(new TestCase("/Sites", directory, false));
        cases.add(new TestCase("/Sites/Site", directory, false));
        cases.add(new TestCase("/Sites/Site/Drives", directory, false));
        cases.add(new TestCase("/Sites/Site/Drives/Drive", directory, false));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Folder/Test", file, true));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Test", file, true));
        cases.add(new TestCase("/Sites/Site/Sites", directory, false));
        cases.add(new TestCase("/Sites/Site/Sites/Site", directory, false));

        // Move/Copy-Tests
        // Assumption: Cyberduck always uses target directory without name to indicate drag.
        // Target always is folder.
        cases.add(new TestCase("/Default/Drives/Drive/Nested/Test", file, "/Default/Drives/Drive", true));
        cases.add(new TestCase("/Default/Drives/Drive/Test", file, "/Default/Drives/Drive/Nested", true));
        cases.add(new TestCase("/Default/Drives/Drive/Test", file, "/Default/Drives/Other Drive", false));
        cases.add(new TestCase("/Default/Drives/Drive/Test", file, "/Groups/Group/Drive", false));
        cases.add(new TestCase("/Default/Drives/Drive/Test", file, "/Sites/Site/Drives/Drive", false));
        cases.add(new TestCase("/Groups/Group/Drive/Nested/Test", file, "/Groups/Group/Drive", true));
        cases.add(new TestCase("/Groups/Group/Drive/Test", file, "/Groups/Group/Drive/Test/Nested", true));
        cases.add(new TestCase("/Groups/Group/Drive/Test", file, "/Groups/Group/Other Drive", false));
        cases.add(new TestCase("/Groups/Group/Drive/Test", file, "/Groups/Other Group/Drive", false));
        cases.add(new TestCase("/Groups/Group/Drive/Test", file, "/Sites/Site/Drives/Drive", false));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Nested/Test", file, "/Sites/Site/Drives/Drive", true));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Test", file, "/Sites/Other Site/Drives/Drive", false));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Test", file, "/Sites/Site/Drives/Drive/Nested", true));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Test", file, "/Sites/Site/Drives/Other Drive", false));

        test(feature, cases);
    }

    @Test
    public void testOneDrive() {
        final OneDriveSession session = new OneDriveSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Object feature = session.getFeature(this.feature);
        final List<TestCase> cases = new ArrayList<>();

        // Rename Tests
        // Assumption: Cyberduck always uses same source and target path to indicate rename.
        cases.add(new TestCase("/Test", directory, false));
        cases.add(new TestCase("/My Files", directory, false));
        cases.add(new TestCase("/Shared", directory, false));
        cases.add(new TestCase("/Shared/Element", file, false));
        cases.add(new TestCase("/My Files/Element", file, true));
        cases.add(new TestCase("/My Files/Folder/Element", file, true));
        cases.add(new TestCase("/Shared/Folder/Element", file, true));

        // Move/Copy-Tests
        // Assumption: Cyberduck always uses target directory without name to indicate drag.
        // Target always is folder.
        cases.add(new TestCase("/My Files", directory, "/Shared", false));
        cases.add(new TestCase("/Shared", directory, "/My Files", false));
        cases.add(new TestCase("/Shared/Element", file, "/My Files", false));
        cases.add(new TestCase("/Shared/Element", file, "/My Files/Folder", false));
        cases.add(new TestCase("/Shared/Element", file, "/Shared/Folder", false));
        cases.add(new TestCase("/Shared/Element", file, "/Shared/Folder/Folder", false));
        cases.add(new TestCase("/Shared/Folder/Element", file, "/Shared", false));
        // CAUTION! This is moving a file from /Shared/Folder to /Shared/Test
        cases.add(new TestCase("/Shared/Folder/Element", file, "/Shared/Test", true));
        cases.add(new TestCase("/Shared/Folder/Element", file, "/My Files", false));
        cases.add(new TestCase("/Shared/Folder/Element", file, "/My Files/Element", false));
        cases.add(new TestCase("/My Files/Element", file, "/My Files/Folder", true));
        cases.add(new TestCase("/My Files/Folder/Element", file, "/My Files", true));
        cases.add(new TestCase("/Shared/Folder/Element", file, "/Shared/Folder", true));
        cases.add(new TestCase("/Shared/Folder/Element", file, "/Shared/Folder/Folder", true));

        test(feature, cases);
    }

    void test(final Object feature, final List<TestCase> test) {
        for(TestCase testCase : test) {
            final Path from = new Path(testCase.source, EnumSet.of(testCase.sourceType));
            final Path to = new Path(testCase.target, EnumSet.of(testCase.targetType));

            if(feature instanceof Move) {
                final Move move = (Move) feature;
                assertEquals(String.format("Move \"%s\" to \"%s\".", from, to), testCase.isValid, move.isSupported(from, to));
            }
            else if(feature instanceof Copy) {
                final Copy copy = (Copy) feature;
                assertEquals(String.format("Copy \"%s\" to \"%s\".", from, to), testCase.isValid, copy.isSupported(from, to));
            }
            else {
                fail();
            }
        }
    }

    static class TestCase {
        final String source;
        final String target;
        final AbstractPath.Type sourceType;
        final AbstractPath.Type targetType;
        final boolean isValid;

        public TestCase(final String rename, final AbstractPath.Type type, final boolean isValid) {
            this(rename, type, rename, type, isValid);
        }

        public TestCase(final String source, final AbstractPath.Type sourceType, final String target, final boolean isValid) {
            this(source, sourceType, target, directory, isValid);
        }

        TestCase(final String source, final AbstractPath.Type sourceType, final String target, final AbstractPath.Type targetType, final boolean isValid) {
            this.source = source;
            this.target = target;
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.isValid = isValid;
        }
    }
}
