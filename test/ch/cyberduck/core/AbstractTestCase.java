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

import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.ui.cocoa.UserDefaultsDateFormatter;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.resources.NSImageIconCache;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
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

    private ActionOperationBatcher pool;

    @Before
    public void pool() {
        pool = ActionOperationBatcherFactory.get();
    }

    protected static Properties properties = System.getProperties();

    @BeforeClass
    public static void properties() throws IOException {
        final InputStream stream = AbstractTestCase.class.getResourceAsStream("/test.properties");
        if(null != stream) {
            // Found in test resources
            properties.load(stream);
        }
        IOUtils.closeQuietly(stream);
    }

    @BeforeClass
    public static void setup() {
        Locale.setDefault(Locale.ENGLISH);
        AutoreleaseActionOperationBatcher.register();
        FinderLocal.register();
        BundleLocale.register();
        NSImageIconCache.register();
        PlistDeserializer.register();
        PlistSerializer.register();
        PlistWriter.register();
        HostPlistReader.register();
        TransferPlistReader.register();
        ProfilePlistReader.register();
        ProtocolFactory.register();
        LaunchServicesApplicationFinder.register();
        UserDefaultsDateFormatter.register();
        WorkspaceApplicationLauncher.register();
        SystemConfigurationProxy.register();
        TemporaryFileService.register();
        LaunchServicesFileDescriptor.register();
    }

    @Before
    public void preferences() {
        Preferences.instance().setProperty("application.support.path", System.getProperty("java.io.tmpdir"));
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("ch.cyberduck").setLevel(Level.DEBUG);
    }

    @After
    public void post() {
        pool.operate();
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