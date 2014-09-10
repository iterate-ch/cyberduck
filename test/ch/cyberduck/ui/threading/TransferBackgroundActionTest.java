package ch.cyberduck.ui.threading;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.ui.AbstractController;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class TransferBackgroundActionTest extends AbstractTestCase {

    @Test
    public void testDuplicate() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch") {
            @Override
            public Credentials getCredentials() {
                return new Credentials(
                        properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
                ) {
                    @Override
                    public void setPassword(final String pass) {
                        //
                    }
                };
            }
        };
        final Path test = new Path("/home/jenkins/transfer/test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(5L);

        final Path copy = new Path(new Path("/home/jenkins/transfer", EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CopyTransfer t = new CopyTransfer(host, host, Collections.<Path, Path>singletonMap(test, copy));

        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AtomicBoolean start = new AtomicBoolean();
        final AtomicBoolean stop = new AtomicBoolean();
        final TransferBackgroundAction action = new TransferBackgroundAction(controller, new SFTPSession(host), new TransferListener() {
            @Override
            public void start(final Transfer transfer) {
                assertEquals(t, transfer);
                start.set(true);
            }

            @Override
            public void stop(final Transfer transfer) {
                assertEquals(t, transfer);
                stop.set(true);
            }

            @Override
            public void progress(final TransferProgress status) {
                //
            }
        }, controller, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback());
        action.prepare();
        action.call();
        assertTrue(t.getDestination().isConnected());
        action.finish();
        assertTrue(start.get());
        assertTrue(stop.get());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());
    }

    @Test
    public void testCopyBetweenHosts() throws Exception {
        final SFTPSession session = new SFTPSession(new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        )));

        final FTPSession destination = new FTPSession(new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        )));

        final Path test = new Path("/home/jenkins/transfer/test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(5L);

        final Path copy = new Path(new Path("/transfer", EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new CopyTransfer(session.getHost(), destination.getHost(),
                Collections.<Path, Path>singletonMap(test, copy));

        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AtomicBoolean start = new AtomicBoolean();
        final AtomicBoolean stop = new AtomicBoolean();
        final TransferBackgroundAction action = new TransferBackgroundAction(controller, session, new TransferListener() {
            @Override
            public void start(final Transfer transfer) {
                assertEquals(t, transfer);
                start.set(true);
            }

            @Override
            public void stop(final Transfer transfer) {
                assertEquals(t, transfer);
                stop.set(true);
            }

            @Override
            public void progress(final TransferProgress status) {
                //
            }
        }, controller, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback());
        action.prepare();
        action.call();
        action.finish();
        assertTrue(start.get());
        assertTrue(stop.get());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());
    }

    @Test
    public void testResumeOnAutomatedRetry() throws Exception {
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AtomicBoolean start = new AtomicBoolean();
        final AtomicBoolean stop = new AtomicBoolean();
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch");
        final SFTPSession session = new SFTPSession(host);
        final TransferOptions options = new TransferOptions();
        final TransferBackgroundAction action = new TransferBackgroundAction(controller, session, new TransferAdapter(),
                new DisabledProgressListener(),
                new DownloadTransfer(host, Collections.singletonList(new TransferItem(new Path("/home/test", EnumSet.of(Path.Type.file)), new NullLocal("/t")))),
                options, new DisabledTransferPrompt(), new DisabledTransferErrorCallback()) {
            @Override
            protected boolean connect(final Session session) throws BackgroundException {
                return false;
            }
        };
        assertEquals(false, options.resumeRequested);
        action.pause();
        assertEquals(true, options.resumeRequested);
    }
}
