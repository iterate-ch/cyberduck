package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MoveWorkerTest {

    @Test
    public void testCompile() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Move.class) {
                    return (T) new Move() {
                        private final AtomicInteger count = new AtomicInteger();

                        @Override
                        public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
                            if(count.get() == 0) {
                                assertEquals(new Path("/t/d/b", EnumSet.of(Path.Type.file)), file);
                                assertEquals(new Path("/t2/d/b", EnumSet.of(Path.Type.file)), renamed);
                            }
                            if(count.get() == 1) {
                                assertEquals(new Path("/t/d", EnumSet.of(Path.Type.directory)), file);
                                assertEquals(new Path("/t2/d", EnumSet.of(Path.Type.directory)), renamed);
                            }
                            if(count.get() == 2) {
                                assertEquals(new Path("/t/a", EnumSet.of(Path.Type.file)), file);
                                assertEquals(new Path("/t2/a", EnumSet.of(Path.Type.file)), renamed);
                            }
                            if(count.get() == 3) {
                                assertEquals(new Path("/t", EnumSet.of(Path.Type.directory)), file);
                                assertEquals(new Path("/t2", EnumSet.of(Path.Type.directory)), renamed);
                            }
                            count.incrementAndGet();
                            return renamed;
                        }

                        @Override
                        public boolean isRecursive(final Path source, final Path target) {
                            return false;
                        }

                        @Override
                        public boolean isSupported(final Path source, final Path target) {
                            return true;
                        }

                        @Override
                        public Move withDelete(final Delete delete) {
                            return this;
                        }
                    };
                }
                return (T) super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/t", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/a", EnumSet.of(Path.Type.file)),
                            new Path("/t/d", EnumSet.of(Path.Type.directory))
                    ));
                }
                if(file.equals(new Path("/t/d", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/d/b", EnumSet.of(Path.Type.file))
                    ));
                }
                if(file.equals(new Path("/", EnumSet.of(Path.Type.directory)))) {
                    return AttributedList.emptyList();
                }
                if(file.equals(new Path("/t2", EnumSet.of(Path.Type.directory)))) {
                    return AttributedList.emptyList();
                }
                if(file.equals(new Path("/t2/d", EnumSet.of(Path.Type.directory)))) {
                    return AttributedList.emptyList();
                }
                fail();
                return null;
            }
        };
        final MoveWorker worker = new MoveWorker(
                Collections.singletonMap(new Path("/t", EnumSet.of(Path.Type.directory)), new Path("/t2", EnumSet.of(Path.Type.directory))),
            PathCache.empty(), PasswordStoreFactory.get(), new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledProgressListener(), new DisabledTranscriptListener());
        final Collection<Path> targets = worker.run(session).values();
        assertEquals(4, targets.size());
        assertTrue(targets.contains(new Path("/t2", EnumSet.of(Path.Type.directory))));
        assertTrue(targets.contains(new Path("/t2/a", EnumSet.of(Path.Type.file))));
        assertTrue(targets.contains(new Path("/t2/d", EnumSet.of(Path.Type.directory))));
        assertTrue(targets.contains(new Path("/t2/d/b", EnumSet.of(Path.Type.file))));
    }
}
