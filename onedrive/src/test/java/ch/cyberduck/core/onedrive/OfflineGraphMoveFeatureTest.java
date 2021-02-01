package ch.cyberduck.core.onedrive;/*
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
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OfflineGraphMoveFeatureTest {
    @Test
    public void testSharepoint() {
        final SharepointSession session = new SharepointSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Move move = session.getFeature(Move.class);
        final List<TestCase> cases = new ArrayList<>();
        // cannot rename root
        cases.add(new TestCase("/", "/", false));
        // cannot rename Documents to root
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/Documents/", "/", false));
        // Cannot rename Drive
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/Documents/", false));
        // can rename drive subitems
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/Documents/Subfolder/", true));
        // cannot move to root
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/Documents/Subfolder/", "/", false));
        // cannot move Drives to Root
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/", "/", false));
        // cannot rename Drives
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/", false));
        // Cannot rename Site
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/", false));
        // cannot rename Sites
        cases.add(new TestCase("/Sites/", false));
        // cannot move group Documents to root
        cases.add(new TestCase("/Groups/Group Name/Documents/", "/", false));
        // cannot rename group Drive
        cases.add(new TestCase("/Groups/Group Name/Documents/", false));
        // can rename in group drive
        cases.add(new TestCase("/Groups/Group Name/Documents/Subfolder/", true));
        // cannot move to root
        cases.add(new TestCase("/Groups/Group Name/Documents/Subfolder/", "/", false));
        // cannot rename group
        cases.add(new TestCase("/Groups/Group Name/", false));
        // cannot rename groups
        cases.add(new TestCase("/Groups/", false));
        // cannot move over drive boundary
        cases.add(new TestCase("/Sites/tenant.sharepoint.com/Drives/Documents/Folder", "/Groups/Group Name/Documents/Folder", false));

        for(TestCase testCase : cases) {
            final Path from = new Path(testCase.getSource(), EnumSet.of(Path.Type.directory));
            final Path to = new Path(testCase.getTarget(), EnumSet.of(Path.Type.directory));

            assertEquals(String.format("Move \"%s\" to \"%s\".", from.getAbsolute(), to.getAbsolute()), testCase.isValid(), move.isSupported(from, to));
        }
    }

    @Test
    public void testOneDrive() {
        final OneDriveSession session = new OneDriveSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Move move = session.getFeature(Move.class);
        final List<TestCase> cases = new ArrayList<>();
        // cannot rename root
        cases.add(new TestCase("/", "/", false));
        // cannot move My Files to root
        cases.add(new TestCase("/My Files/", "/", false));
        // cannot rename My Files
        cases.add(new TestCase("/My Files/", false));
        // cannot move folder to root
        cases.add(new TestCase("/My Files/Folder", "/", false));
        // can move rename folder
        cases.add(new TestCase("/My Files/Folder", "/My Files/Sub Folder/Folder 2", true));
        // cannot rename Shared
        cases.add(new TestCase("/Shared/", false));
        // cannot rename item in Shared
        cases.add(new TestCase("/Shared/Folder", false));
        // can rename item in folder in Shared
        cases.add(new TestCase("/Shared/Folder/Item", true));

        for(TestCase testCase : cases) {
            final Path from = new Path(testCase.getSource(), EnumSet.of(Path.Type.directory));
            final Path to = new Path(testCase.getTarget(), EnumSet.of(Path.Type.directory));

            assertEquals(String.format("Move \"%s\" to \"%s\".", from, to), testCase.isValid(), move.isSupported(from, to));
        }
    }

    private static class TestCase {
        private final String source;
        private final String target;
        private final boolean isValid;

        public TestCase(final String rename, final boolean isValid) {
            source = rename;
            target = rename;
            this.isValid = isValid;
        }

        public TestCase(final String source, final String target, final boolean isValid) {
            this.source = source;
            this.target = target;
            this.isValid = isValid;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
