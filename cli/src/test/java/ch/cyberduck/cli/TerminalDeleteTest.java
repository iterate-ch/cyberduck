package ch.cyberduck.cli;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Wildcard handling of {@code duck --delete}. The session supports recursive delete, which is the case where
 * passing the parent folder to {@link ch.cyberduck.core.worker.DeleteWorker} deleted it with all its contents.
 */
public class TerminalDeleteTest {

    private static final Path PARENT = new Path("/remote", EnumSet.of(Path.Type.directory));

    @Test
    public void testDeleteWildcardOnlyMatchingChildren() throws Exception {
        final RecordingSession session = new RecordingSession(new AttributedList<>(Arrays.asList(
                new Path(PARENT, "a.txt", EnumSet.of(Path.Type.file)),
                new Path(PARENT, "b.txt", EnumSet.of(Path.Type.file)),
                new Path(PARENT, "c.jpg", EnumSet.of(Path.Type.file)),
                new Path(PARENT, "txt", EnumSet.of(Path.Type.directory))
        )));
        assertEquals(Terminal.Exit.success, this.delete(session, "/remote/*.txt"));
        assertEquals(Arrays.asList(
                new Path(PARENT, "a.txt", EnumSet.of(Path.Type.file)),
                new Path(PARENT, "b.txt", EnumSet.of(Path.Type.file))
        ), session.deleted);
        assertFalse(session.deleted.contains(PARENT));
        assertEquals(Collections.singletonList(PARENT), session.listed);
    }

    @Test
    public void testDeleteWildcardSkipsDuplicateAndTrashed() throws Exception {
        final RecordingSession session = new RecordingSession(new AttributedList<>(Arrays.asList(
                new Path(PARENT, "a.txt", EnumSet.of(Path.Type.file)),
                new Path("/remote/previous.txt", EnumSet.of(Path.Type.file), new DefaultPathAttributes().setVersionId("1").setDuplicate(true)),
                new Path("/remote/trashed.txt", EnumSet.of(Path.Type.file), new DefaultPathAttributes().setTrashed(true))
        )));
        assertEquals(Terminal.Exit.success, this.delete(session, "/remote/*.txt"));
        assertEquals(Collections.singletonList(new Path(PARENT, "a.txt", EnumSet.of(Path.Type.file))), session.deleted);
    }

    @Test
    public void testDeleteWildcardNoMatch() throws Exception {
        final RecordingSession session = new RecordingSession(new AttributedList<>(Arrays.asList(
                new Path(PARENT, "a.jpg", EnumSet.of(Path.Type.file)),
                new Path(PARENT, "b", EnumSet.of(Path.Type.directory))
        )));
        assertEquals(Terminal.Exit.success, this.delete(session, "/remote/*.txt"));
        assertTrue(session.deleted.isEmpty());
        assertFalse(session.invoked);
    }

    @Test
    public void testDeleteWildcardListingFailure() throws Exception {
        final RecordingSession session = new RecordingSession(null);
        assertEquals(Terminal.Exit.failure, this.delete(session, "/remote/*.txt"));
        assertTrue(session.deleted.isEmpty());
        assertFalse(session.invoked);
    }

    @Test
    public void testDeleteWithoutWildcard() throws Exception {
        final RecordingSession session = new RecordingSession(null);
        assertEquals(Terminal.Exit.success, this.delete(session, "/remote/a.txt"));
        assertEquals(Collections.singletonList(new Path(PARENT, "a.txt", EnumSet.of(Path.Type.file))), session.deleted);
        assertTrue(session.listed.isEmpty());
    }

    private Terminal.Exit delete(final RecordingSession session, final String remote) throws Exception {
        final Options options = TerminalOptionsBuilder.options();
        final CommandLine input = new DefaultParser().parse(options, new String[]{
                "--assumeyes", "--quiet", "--delete", String.format("test://localhost%s", remote)});
        final TerminalPreferences preferences = new TerminalPreferences(new MemoryPreferences());
        preferences.load();
        preferences.setFactories();
        preferences.setDefaults();
        final Terminal terminal = new Terminal(new ProtocolFactory(new HashSet<>(Collections.singletonList(new TestProtocol()))),
                preferences, options, input);
        return terminal.delete(new SessionPool.SingleSessionPool(session), new Path(remote, EnumSet.of(Path.Type.file)));
    }

    /**
     * Lists fixed children of {@link #PARENT} and records paths passed to the delete feature
     */
    private static final class RecordingSession extends NullSession {
        /**
         * Children of parent folder or null to fail listing
         */
        private final AttributedList<Path> children;
        private final List<Path> listed = new ArrayList<>();
        private final List<Path> deleted = new ArrayList<>();
        private boolean invoked;

        public RecordingSession(final AttributedList<Path> children) {
            super(new Host(new TestProtocol()));
            this.children = children;
        }

        @Override
        public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
            listed.add(folder);
            if(null == children || !folder.equals(PARENT)) {
                throw new AccessDeniedException();
            }
            return children;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T _getFeature(final Class<T> type) {
            if(type == Delete.class) {
                return (T) new Delete() {
                    @Override
                    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                        invoked = true;
                        deleted.addAll(files.keySet());
                    }

                    @Override
                    public EnumSet<Flags> features(final Path file) {
                        return EnumSet.of(Flags.recursive);
                    }
                };
            }
            return super._getFeature(type);
        }
    }
}
