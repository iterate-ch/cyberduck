package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.PermissionOverwrite;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WritePermissionWorkerTest {

    @Test
    public void testRun() throws Exception {
        final PermissionOverwrite permission = new PermissionOverwrite(
                new PermissionOverwrite.Action(true, true, true),
                new PermissionOverwrite.Action(null, false, false),
                new PermissionOverwrite.Action(true, false, false)
        );
        // Tests all actions set to read
        final Path path = new Path("a", EnumSet.of(Path.Type.directory), new DefaultPathAttributes().setPermission(
                new StaticPermission(Permission.Action.read, Permission.Action.read, Permission.Action.read)));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(path), permission, new BooleanRecursiveCallback<>(true), ProgressListener.noop);
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<>();
                // test just group set to read
                children.add(new Path("b", EnumSet.of(Path.Type.file), new DefaultPathAttributes().setPermission(
                        new StaticPermission(Permission.Action.none, Permission.Action.read, Permission.Action.none))));
                return children;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == UnixPermission.class) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final TransferStatus status) throws BackgroundException {
                            assertEquals(new StaticPermission(744), status.getPermission());
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }

    @Test
    @Ignore
    public void testRunRecursiveRetainDirectoryExecute() throws Exception {
        final PermissionOverwrite permission = new PermissionOverwrite(
                new PermissionOverwrite.Action(true, true, false),
                new PermissionOverwrite.Action(false, true, false),
                new PermissionOverwrite.Action(false, true, false)
        );
        final Path a = new Path("a", EnumSet.of(Path.Type.directory), new DefaultPathAttributes().setPermission(
                new StaticPermission(Permission.Action.all, Permission.Action.all, Permission.Action.all)));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(a), permission, new BooleanRecursiveCallback<>(true), ProgressListener.noop);
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(a)) {
                    final AttributedList<Path> children = new AttributedList<>();
                    final Path d = new Path("d", EnumSet.of(Path.Type.directory));
                    d.attributes().setPermission(new StaticPermission(744));
                    children.add(d);
                    children.add(new Path("f", EnumSet.of(Path.Type.file)));
                    return children;
                }
                return AttributedList.emptyList();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == UnixPermission.class) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final TransferStatus status) {
                            switch(file.getName()) {
                                case "a":
                                    assertEquals(new StaticPermission(644), status.getPermission());
                                    break;
                                case "d":
                                    assertEquals(new StaticPermission(544), status.getPermission());
                                    break;
                                case "f":
                                    assertEquals(new StaticPermission(644), status.getPermission());
                                    break;
                                default:
                                    fail();
                                    break;
                            }
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }

    @Test
    @Ignore
    public void testRunRecursiveSetDirectoryExecute() throws Exception {
        final Path a = new Path("a", EnumSet.of(Path.Type.directory));
        a.attributes().setPermission(new StaticPermission(774));
        final Path f = new Path("f", EnumSet.of(Path.Type.file));
        final Path d = new Path("d", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(a), new PermissionOverwrite(
                new PermissionOverwrite.Action(true, true, true),
                new PermissionOverwrite.Action(false, true, true),
                new PermissionOverwrite.Action(true, false, true)
        ), new BooleanRecursiveCallback<>(true), ProgressListener.noop);
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(a)) {
                    final AttributedList<Path> children = new AttributedList<>();
                    d.attributes().setPermission(new StaticPermission(774));
                    children.add(d);
                    d.attributes().setPermission(new StaticPermission(666));
                    children.add(f);
                    return children;
                }
                return AttributedList.emptyList();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == UnixPermission.class) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final TransferStatus status) throws BackgroundException {
                            if(file.equals(a)) {
                                assertEquals(file.toString(), new StaticPermission(775), status.getPermission());
                            }
                            if(file.equals(d)) {
                                assertEquals(file.toString(), new StaticPermission(775), status.getPermission());
                            }
                            if(file.equals(f)) {
                                assertEquals(file.toString(), new StaticPermission(664), status.getPermission());
                            }
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }

    @Test
    public void testRetainStickyBit() throws Exception {
        final PermissionOverwrite permission = new PermissionOverwrite(
                new PermissionOverwrite.Action(true, true, true),
                new PermissionOverwrite.Action(null, false, false),
                new PermissionOverwrite.Action(true, false, false)
        );
        final Path path = new Path("a", EnumSet.of(Path.Type.directory));
        path.attributes().setPermission(new StaticPermission(Permission.Action.none, Permission.Action.read, Permission.Action.none,
                true, false, false));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(path), permission, new BooleanRecursiveCallback<>(true), ProgressListener.noop);
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<>();
                // File has all set
                children.add(new Path("b", EnumSet.of(Path.Type.file), new DefaultPathAttributes().setPermission(
                        new StaticPermission(Permission.Action.all, Permission.Action.all, Permission.Action.all))));
                return children;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == UnixPermission.class) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final TransferStatus status) {
                            assertEquals(new StaticPermission(1744), status.getPermission());
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }
}
