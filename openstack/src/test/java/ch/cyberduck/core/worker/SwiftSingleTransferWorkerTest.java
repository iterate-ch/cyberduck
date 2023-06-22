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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.notification.DisabledNotificationService;
import ch.cyberduck.core.openstack.SwiftAttributesFinderFeature;
import ch.cyberduck.core.openstack.SwiftDeleteFeature;
import ch.cyberduck.core.openstack.SwiftLargeObjectUploadFeature;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.openstack.SwiftRegionService;
import ch.cyberduck.core.openstack.SwiftSession;
import ch.cyberduck.core.openstack.SwiftWriteFeature;
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
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.test.VaultTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftSingleTransferWorkerTest extends VaultTest {

    @Test
    public void testUploadTransferWithFailure() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = new byte[2 * 1024 * 1024];
        new Random().nextBytes(content);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                PROPERTIES.get("rackspace.user"), PROPERTIES.get("rackspace.password")
        )) {
            @Override
            public String getProperty(final String key) {
                if("openstack.upload.largeobject.size".equals(key)) {
                    return String.valueOf(1048576);
                }
                if("openstack.upload.largeobject.concurrency".equals(key)) {
                    return String.valueOf(5);
                }
                return super.getProperty(key);
            }
        };
        final AtomicBoolean failed = new AtomicBoolean();
        final SwiftSession session = new SwiftSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Upload.class) {
                    final SwiftRegionService regionService = new SwiftRegionService(this);
                    return (T) new SwiftLargeObjectUploadFeature(this, regionService, new SwiftWriteFeature(this, regionService), 1024L * 1024L, 5) {
                        @Override
                        protected InputStream decorate(final InputStream in, final MessageDigest digest) {
                            if(failed.get()) {
                                // Second attempt successful
                                return in;
                            }
                            return new CountingInputStream(in) {
                                @Override
                                protected void beforeRead(final int n) throws IOException {
                                    super.beforeRead(n);
                                    if(this.getByteCount() > 1024L * 1024L) {
                                        failed.set(true);
                                        throw new SocketTimeoutException();
                                    }
                                }
                            };
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        new LoginConnectionService(new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Transfer t = new UploadTransfer(session.getHost(), test, local);
        final BytecountStreamListener counter = new BytecountStreamListener();
        assertTrue(new SingleTransferWorker(session, session, t, new TransferOptions(), new TransferSpeedometer(t), new DisabledTransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem file) {
                return TransferAction.overwrite;
            }
        }, new DisabledTransferErrorCallback(),
            new DisabledProgressListener(), counter, new DisabledLoginCallback(), new DisabledNotificationService()) {

        }.run(session));
        local.delete();
        assertTrue(t.isComplete());
        assertEquals(content.length, new SwiftAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, counter.getRecv(), 0L);
        assertEquals(content.length, counter.getSent(), 0L);
        assertTrue(failed.get());
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
