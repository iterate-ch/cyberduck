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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Touch;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OfflineGraphDirectoryTouchFeatureTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection features() {
        return Arrays.asList(Touch.class, Directory.class);
    }

    @Parameterized.Parameter
    public Class feature;

    @Test
    public void testSharepoint() {
        final SharepointSession session = new SharepointSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Object feature = session.getFeature(this.feature);
        final List<TestCase> cases = new ArrayList<>();

        // Validate Touch.isSupported for valid targets only.
        // Valid targets: Any path in a drive.
        // Sites, Groups, default etc. are not valid targets for file creation.
        // Usually accessible:
        // (Sites/<SiteName>/)Drives/<Drive Name>
        // <Group Name>/<Drive Name>

        cases.add(new TestCase("/Default/Drives/Drive", false));
        cases.add(new TestCase("/Default/Drives/Drive/Test", true));
        cases.add(new TestCase("/Default/Test", false));
        cases.add(new TestCase("/Groups/Group/Drives/Test", true));
        cases.add(new TestCase("/Groups/Group/Test", false));
        cases.add(new TestCase("/Groups/Test", false));
        cases.add(new TestCase("/Invalid/Test", false));
        cases.add(new TestCase("/Sites/Site/Drives/Drive/Test", true));
        cases.add(new TestCase("/Sites/Site/Drives/Test", false));
        cases.add(new TestCase("/Sites/Site/Sites/Site/Drives/Drive/Test", true));
        cases.add(new TestCase("/Sites/Site/Sites/Site/Drives/Test", false));
        cases.add(new TestCase("/Sites/Site/Sites/Site/Test", false));
        cases.add(new TestCase("/Sites/Site/Sites/Test", false));
        cases.add(new TestCase("/Sites/Site/Test", false));
        cases.add(new TestCase("/Sites/Test", false));
        cases.add(new TestCase("/Test", false));

        test(feature, cases);
    }

    @Test
    public void testOneDrive() {
        final OneDriveSession session = new OneDriveSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Object feature = session.getFeature(this.feature);
        final List<TestCase> cases = new ArrayList<>();

        // Validate Touch.isSupported for valid targets only.
        // Valid targets: Any path in /My Files or in a directory of a shared folder.

        cases.add(new TestCase("/Invalid/Test", false));
        cases.add(new TestCase("/My Files/Folder/Test", true));
        cases.add(new TestCase("/My Files/Test", true));
        cases.add(new TestCase("/Shared/Shared/Test", true));
        cases.add(new TestCase("/Shared/Test", false));
        cases.add(new TestCase("/Test", false));

        test(feature, cases);
    }

    private void test(final Object feature, final List<TestCase> test) {
        for(TestCase testCase : test) {
            final Path target = new Path(testCase.target, EnumSet.of(Path.Type.file));
            final Path parent = target.getParent();
            final String name = target.getName();

            if(feature instanceof Touch) {
                final Touch touch = (Touch) feature;
                assertEquals(String.format("Create \"%s\" in \"%s\".", name, parent.getAbsolute()), testCase.isValid, touch.isSupported(parent, name));
            }
            else if(feature instanceof Directory) {
                final Directory directory = (Directory) feature;
                assertEquals(String.format("Create \"%s\" in \"%s\".", name, parent.getAbsolute()), testCase.isValid, directory.isSupported(parent, name));
            }
            else {
                fail();
            }
        }
    }

    static class TestCase {
        private final String target;
        private final boolean isValid;

        TestCase(final String target, final boolean isValid) {
            this.target = target;
            this.isValid = isValid;
        }
    }
}
