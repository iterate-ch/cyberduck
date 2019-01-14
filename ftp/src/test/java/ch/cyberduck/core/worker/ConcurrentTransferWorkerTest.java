package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.pool.DefaultSessionPool;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ConcurrentTransferWorkerTest extends AbstractFTPTest {

    @Test
    public void testConcurrentSessions() throws Exception {
        final int files = 20;
        final int connections = 2;
        final List<TransferItem> list = new ArrayList<TransferItem>();
        final Local file = new Local(File.createTempFile(UUID.randomUUID().toString(), "t").getAbsolutePath());
        for(int i = 1; i <= files; i++) {
            list.add(new TransferItem(new Path(String.format("/t%d", i), EnumSet.of(Path.Type.file)), file));
        }
        final Transfer transfer = new UploadTransfer(session.getHost(), list);
        final DefaultSessionPool pool = new DefaultSessionPool(
            new LoginConnectionService(new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    return new Credentials(username, "test");
                }

                @Override
                public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                    //
                }
            }, new DisabledHostKeyCallback(), new DisabledPasswordStore(),
                new DisabledProgressListener()),
            new DisabledX509TrustManager(), new DefaultX509KeyManager(),
            new DefaultVaultRegistry(new DisabledPasswordCallback()), PathCache.empty(), new DisabledTranscriptListener(), session.getHost());
        final ConcurrentTransferWorker worker = new ConcurrentTransferWorker(
            pool.withMaxTotal(connections), SessionPool.DISCONNECTED,
            transfer, new TransferOptions(), new TransferSpeedometer(transfer), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledLoginCallback(), new DisabledPasswordCallback(), new DisabledProgressListener(), new DisabledStreamListener(), new DisabledNotificationService()
        );
        pool.withMaxTotal(connections);
        final Session<?> s = worker.borrow(ConcurrentTransferWorker.Connection.source);
        final Session<?> session = worker.borrow(ConcurrentTransferWorker.Connection.source);
        assertTrue(worker.run());
        worker.release(session, ConcurrentTransferWorker.Connection.source, null);
        worker.release(s, ConcurrentTransferWorker.Connection.source, null);
        assertEquals(0L, transfer.getTransferred(), 0L);
        worker.cleanup(true);
    }
}
