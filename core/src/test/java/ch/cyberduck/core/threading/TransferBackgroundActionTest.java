/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.worker.ConcurrentTransferWorker;

import org.junit.Test;

import java.net.SocketException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class TransferBackgroundActionTest {

    @Test
    public void testWorkerImplementationDefaultConcurrent() throws Exception {
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final Host host = new Host(new TestProtocol(), "l");
        host.setTransfer(Host.TransferType.concurrent);
        assertEquals(ConcurrentTransferWorker.class, new TransferBackgroundAction(controller, new SingleSessionPool(
                new TestLoginConnectionService(), new NullSession(host), PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()), SessionPool.DISCONNECTED,
                new TransferAdapter(), new UploadTransfer(host, Collections.emptyList()), new TransferOptions()).worker.getClass());
        assertEquals(ConcurrentTransferWorker.class, new TransferBackgroundAction(controller, new SingleSessionPool(
                new TestLoginConnectionService(), new NullSession(host), PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()), SessionPool.DISCONNECTED,
                new TransferAdapter(), new DownloadTransfer(host, Collections.emptyList()), new TransferOptions()).worker.getClass());
    }

    @Test
    public void testDuplicate() throws Exception {
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch") {
            @Override
            public Credentials getCredentials() {
                return new Credentials(
                        System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
                ) {
                    @Override
                    public void setPassword(final String pass) {
                        //
                    }
                };
            }
        };
        final Path directory = new Path("/home/jenkins/transfer", EnumSet.of(Path.Type.directory));
        final Path test = new Path(directory, "test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);

        final Path copy = new Path(directory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CopyTransfer t = new CopyTransfer(host, host, Collections.singletonMap(test, copy)) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested, final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
                return TransferAction.overwrite;
            }
        };

        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AtomicBoolean start = new AtomicBoolean();
        final AtomicBoolean stop = new AtomicBoolean();
        final Session session = new NullSession(host);
        final Session destination = new NullSession(host);
        final TransferBackgroundAction action = new TransferBackgroundAction(controller,
                new SingleSessionPool(
                        new TestLoginConnectionService(), session, PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()),
                new SingleSessionPool(
                        new TestLoginConnectionService(), destination, PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()),
                new TransferListener() {
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
                }, t, new TransferOptions());
        action.prepare();
        action.call();
        assertTrue(destination.isConnected());
        action.finish();
        assertNull(action.getException());
        assertTrue(start.get());
        assertTrue(stop.get());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());
    }

    @Test
    public void testCopyBetweenHosts() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol(), "test.cyberduck.ch"));
        final Session destination = new NullSession(new Host(new TestProtocol(), "test.cyberduck.ch"));

        final Path directory = new Path("/home/jenkins/transfer", EnumSet.of(Path.Type.directory));
        final Path test = new Path(directory, "test", EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);

        final Path copy = new Path(new Path("/transfer", EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Transfer t = new CopyTransfer(session.getHost(), destination.getHost(), Collections.singletonMap(test, copy)) {
            @Override
            public TransferAction action(final Session<?> source, final Session<?> destination, final boolean resumeRequested, final boolean reloadRequested, final TransferPrompt prompt, final ListProgressListener listener) throws BackgroundException {
                return TransferAction.overwrite;
            }
        };

        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AtomicBoolean start = new AtomicBoolean();
        final AtomicBoolean stop = new AtomicBoolean();
        final TransferBackgroundAction action = new TransferBackgroundAction(controller,
                new SingleSessionPool(
                        new TestLoginConnectionService(), session, PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()),
                new SingleSessionPool(
                        new TestLoginConnectionService(), destination, PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback()), new TransferListener() {
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
        }, t, new TransferOptions());
        action.prepare();
        action.call();
        action.finish();
        assertNull(action.getException());
        assertTrue(start.get());
        assertTrue(stop.get());
        assertTrue(t.isComplete());
        assertNotNull(t.getTimestamp());
    }

    @Test
    public void testResumeOnRetryWithException() throws Exception {
        final AtomicBoolean alert = new AtomicBoolean();
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }

            @Override
            public boolean alert(final Host host, final BackgroundException failure, final StringBuilder transcript) {
                final boolean alerted = alert.get();
                alert.set(true);
                return !alerted;
            }
        };
        final Host host = new Host(new TestProtocol(), "test.cyberduck.ch");
        final TransferOptions options = new TransferOptions();
        final AtomicBoolean retry = new AtomicBoolean();
        final TransferBackgroundAction action = new TransferBackgroundAction(controller, new DefaultSessionPool(
                new TestLoginConnectionService(), new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DisabledPasswordStore(), new DisabledPasswordCallback(), PathCache.empty(), new DisabledProgressListener(), host) {
            @Override
            public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
                throw new ConnectionRefusedException("d", new SocketException());
            }
        }, SessionPool.DISCONNECTED, new TransferAdapter(),
                new DownloadTransfer(host, Collections.singletonList(new TransferItem(new Path("/home/test", EnumSet.of(Path.Type.file)), new NullLocal("/t")))), options);
        assertFalse(alert.get());
        // Connect, prepare and run
        new BackgroundCallable<Boolean>(action, controller, BackgroundActionRegistry.global()).call();
        assertTrue(alert.get());
        assertNotNull(action.getException());
        assertTrue(options.resumeRequested);
    }
}
