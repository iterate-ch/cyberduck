package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.aquaticprime.Donation;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.ui.cocoa.UserDefaultsPreferences;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.cocoa.i18n.BundleLocale;
import ch.cyberduck.ui.cocoa.serializer.PlistDeserializer;
import ch.cyberduck.ui.cocoa.serializer.PlistSerializer;
import ch.cyberduck.ui.cocoa.serializer.PlistWriter;
import ch.cyberduck.ui.cocoa.serializer.ProtocolPlistReader;
import ch.cyberduck.ui.growl.GrowlNative;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @version $Id$
 */
public class AbstractTestCase {

    static {
        BasicConfigurator.configure();
    }

    NSAutoreleasePool pool;

    @Before
    public void pool() {
        pool = NSAutoreleasePool.push();
    }

    @BeforeClass
    public static void register() {
        AutoreleaseActionOperationBatcher.register();
        FinderLocal.register();
        UserDefaultsPreferences.register();
        BundleLocale.register();
        GrowlNative.register();
        Donation.register();

        PlistDeserializer.register();
        PlistSerializer.register();

        ProtocolPlistReader.register();

        PlistWriter.register();

        ProtocolFactory.register();

        Preferences.instance().setProperty("application.support.path", ".");
    }

    @After
    public void post() {
        pool.drain();
    }


    protected <T> void repeat(final Callable<T> c, int repeat) throws InterruptedException, ExecutionException {
        final ExecutorService service = Executors.newCachedThreadPool();
        final BlockingQueue<Future<T>> queue = new LinkedBlockingQueue<Future<T>>();
        final CompletionService<T> completion = new ExecutorCompletionService<T>(service, queue);
        for(int i = 0; i < repeat; i++) {
            completion.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    final NSAutoreleasePool p = NSAutoreleasePool.push();
                    try {
                        return c.call();
                    }
                    finally {
                        p.drain();
                    }
                }
            });
        }
        for(int i = 0; i < repeat; i++) {
            queue.take().get();
        }
    }
}