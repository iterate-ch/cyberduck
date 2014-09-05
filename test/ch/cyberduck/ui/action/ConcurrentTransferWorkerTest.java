package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

public class ConcurrentTransferWorkerTest extends AbstractTestCase {

    @Test(expected = BackgroundException.class)
    public void testBorrowDnsFailure() throws Exception {
        final Transfer t = new UploadTransfer(new Host("unknownhostname"),
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new LoginConnectionService(new DisabledLoginController(),
                new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener());
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                connection, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        try {
            final Session<?> session = worker.borrow();
        }
        catch(BackgroundException e) {
            assertEquals("DNS lookup for unknownhostname failed. DNS is the network service that translates a server name to its Internet address. This error is most often caused by having no connection to the Internet or a misconfigured network. It can also be caused by an unresponsive DNS server or a firewall preventing access to the network.", e.getDetail());
            assertEquals("Connection failed", e.getMessage());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testBorrowMissingLoginCredentials() throws Exception {
        final Transfer t = new UploadTransfer(new Host("test.cyberduck.ch"),
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new LoginConnectionService(new DisabledLoginController(),
                new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener());
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                connection, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), new DisabledTranscriptListener());
        worker.borrow();
    }

    @Test
    public void testBorrow() throws Exception {
        final Transfer t = new UploadTransfer(new Host("test.cyberduck.ch"),
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new LoginConnectionService(new DisabledLoginController(),
                new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener()) {
            @Override
            public boolean check(Session session, Cache cache) throws BackgroundException {
                return true;
            }

            @Override
            public boolean check(Session session, Cache cache, BackgroundException failure) throws BackgroundException {
                return true;
            }
        };
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                connection, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), new DisabledTranscriptListener(), 2);
        assertNotSame(worker.borrow(), worker.borrow());
    }

    @Test
    public void testSessionReuse() throws Exception {
        final Transfer t = new UploadTransfer(new Host("test.cyberduck.ch"),
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new NullLocal("l"));
        final LoginConnectionService connection = new LoginConnectionService(new DisabledLoginController(),
                new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener()) {
            @Override
            public boolean check(Session session, Cache cache) throws BackgroundException {
                return true;
            }

            @Override
            public boolean check(Session session, Cache cache, BackgroundException failure) throws BackgroundException {
                return true;
            }
        };
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
                connection, t, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback(),
                new DisabledProgressListener(), new DisabledTranscriptListener(), 1);
        final Session<?> session = worker.borrow();
        worker.release(session);
        assertEquals(Session.State.closed, session.getState());
        final Session<?> reuse = worker.borrow();
        assertSame(session, reuse);
        final CyclicBarrier lock = new CyclicBarrier(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assertSame(session, worker.borrow());
                    try {
                        lock.await();
                    }
                    catch(InterruptedException e) {
                        fail();
                    }
                    catch(BrokenBarrierException e) {
                        fail();
                    }
                }
                catch(BackgroundException e) {
                    fail();
                }
            }
        }).start();
        worker.release(reuse);
        lock.await();
    }
}