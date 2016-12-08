package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.PermissionOverwrite;
import ch.cyberduck.core.TestPermissionAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultUnixPermissionFeature;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class ReadPermissionWorkerTest {


    @Test
    public void testRun() throws Exception {
        final ReadPermissionWorker worker = new ReadPermissionWorker(
                Arrays.<Path>asList(
                        new Path("/a", EnumSet.of(Path.Type.file), new TestPermissionAttributes(Permission.Action.all, Permission.Action.all, Permission.Action.none)),
                        new Path("/b", EnumSet.of(Path.Type.file), new TestPermissionAttributes(Permission.Action.all, Permission.Action.read_write, Permission.Action.read)))) {
            @Override
            public void cleanup(final PermissionOverwrite result) {
                //
            }
        };

        PermissionOverwrite overwrite = worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == UnixPermission.class) {
                    return (T) new DefaultUnixPermissionFeature() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) throws BackgroundException {
                            return file.attributes().getPermission();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super._getFeature(type);
            }
        });

        /*
        +-----+-----+-----+
        | rwx | rwx | --- |
        | rwx | rw- | r-- |
        +=====+=====+=====+
        | rwx | rw? | ?-- |
        +-----+-----+-----+
         */

        assertEquals(Boolean.TRUE, overwrite.user.read);
        assertEquals(Boolean.TRUE, overwrite.user.write);
        assertEquals(Boolean.TRUE, overwrite.user.execute);

        assertEquals(Boolean.TRUE, overwrite.group.read);
        assertEquals(Boolean.TRUE, overwrite.group.write);
        assertEquals(null, overwrite.group.execute);

        assertEquals(null, overwrite.other.read);
        assertEquals(Boolean.FALSE, overwrite.other.write);
        assertEquals(Boolean.FALSE, overwrite.other.execute);

        assertEquals("Getting permission of a… (Multiple files) (2)", worker.getActivity());
    }
}
