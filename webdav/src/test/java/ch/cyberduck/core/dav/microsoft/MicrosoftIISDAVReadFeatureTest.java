package ch.cyberduck.core.dav.microsoft;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MicrosoftIISDAVReadFeatureTest extends AbstractMicrosoftIISDAVTest {

    @Test
    public void testReadConcurrency() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final ExecutorService service = Executors.newCachedThreadPool();
        final BlockingQueue<Future<Void>> queue = new LinkedBlockingQueue<>();
        final CompletionService<Void> completion = new ExecutorCompletionService<>(service, queue);
        final int num = 5;
        for(int i = 0; i < num; i++) {
            completion.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    assertTrue(new MicrosoftIISDAVFindFeature(session).find(test));
                    assertEquals(content.length, new MicrosoftIISDAVListService(session, new MicrosoftIISDAVAttributesFinderFeature(session)).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
                    final TransferStatus status = new TransferStatus();
                    status.setLength(-1L);
                    final InputStream in = new MicrosoftIISDAVReadFeature(session).read(test, status, new DisabledConnectionCallback());
                    assertNotNull(in);
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
                    new StreamCopier(status, status).transfer(in, buffer);
                    final byte[] reference = new byte[content.length];
                    System.arraycopy(content, 0, reference, 0, content.length);
                    assertArrayEquals(reference, buffer.toByteArray());
                    in.close();
                    return null;
                }
            });
        }
        for(int i = 0; i < num; i++) {
            completion.take().get();
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
