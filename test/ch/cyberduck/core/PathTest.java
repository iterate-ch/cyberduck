package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

public class PathTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testDictionary() {
        final Session s = SessionFactory.createSession(new Host("localhost"));
        Path path = PathFactory.createPath(s,
                "/path", Path.DIRECTORY_TYPE);
        assertEquals(path, PathFactory.createPath(s, path.getAsDictionary()));
    }

    @Test
    public void testNormalize() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                "/path/to/remove/..", Path.DIRECTORY_TYPE);
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/.././");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/remove/../to/remove/.././");
        assertEquals("/path/to", path.getAbsolute());
//        path.setPath("../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
//        path.setPath("/../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/remove/../../");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/././././to");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("./.path/to");
        assertEquals("/.path/to", path.getAbsolute());
        path.setPath(".path/to");
        assertEquals("/.path/to", path.getAbsolute());
        path.setPath("/path/.to");
        assertEquals("/path/.to", path.getAbsolute());
        path.setPath("/path//to");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path///to////");
        assertEquals("/path/to", path.getAbsolute());


        assertEquals(Path.normalize("relative/path", false), "relative/path");
        assertEquals(Path.normalize("/absolute/path", true), "/absolute/path");
        assertEquals(Path.normalize("/absolute/path", false), "/absolute/path");
    }

    @Test
    public void testName() throws Exception {
        {
            Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                    "/path/to/file/", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                    "/path/to/file", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    @Test
    public void test1067() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                "\\\\directory", Path.DIRECTORY_TYPE);
        assertEquals("\\\\directory", path.getAbsolute());
        assertEquals("/", path.getParent().getAbsolute());
    }

    @Test
    public void test972() throws Exception {
        assertEquals("//home/path", Path.normalize("//home/path"));
    }

    @Test
    public void testTransfer() throws Exception {
        Path p = new NullPath("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        status.setLength(432768L);
        p.transfer(new NullInputStream(status.getLength()), new NullOutputStream(),
                new StreamListener() {
                    long sent;
                    long received;

                    @Override
                    public void bytesSent(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        sent += bytes;
                        assertTrue(sent == received);
                    }

                    @Override
                    public void bytesReceived(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        received += bytes;
                        assertTrue(received > sent);
                    }
                }, -1, status);
        assertTrue(status.isComplete());
        assertTrue(status.getCurrent() == status.getLength());
    }

    @Test
    public void testTransferInterrupt() throws Exception {
        final Path p = new NullPath("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        final CyclicBarrier lock = new CyclicBarrier(2);
        status.setLength(432768L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p.transfer(new NullInputStream(status.getLength()), new NullOutputStream(),
                            new StreamListener() {
                                @Override
                                public void bytesSent(long bytes) {
                                    //
                                }

                                @Override
                                public void bytesReceived(long bytes) {
                                    try {
                                        lock.await();
                                    }
                                    catch(InterruptedException e) {
                                        fail(e.getMessage());
                                    }
                                    catch(BrokenBarrierException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, -1, status);
                }
                catch(IOException e) {
                    fail(e.getMessage());
                }
            }
        }).start();
        lock.await();
        status.setCanceled();
        assertFalse(status.isComplete());
        assertTrue(status.isCanceled());
        assertEquals(32768L, status.getCurrent());
    }
}
