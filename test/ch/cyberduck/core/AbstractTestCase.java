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
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.cocoa.UserDefaultsDateFormatter;
import ch.cyberduck.ui.cocoa.UserDefaultsPreferences;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.growl.GrowlNative;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
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

    private NSAutoreleasePool pool;

    @Before
    public void pool() {
        pool = NSAutoreleasePool.push();
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
        AutoreleaseActionOperationBatcher.register();
        FinderLocal.register();
        UserDefaultsPreferences.register();
        BundleLocale.register();
        GrowlNative.register();
        PlistDeserializer.register();
        PlistSerializer.register();
        PlistWriter.register();
        HostPlistReader.register();
        TransferPlistReader.register();
        ProfilePlistReader.register();
        ProtocolFactory.register();
        LaunchServicesApplicationFinder.register();
        NSObjectPathReference.register();
        UserDefaultsDateFormatter.register();
        WorkspaceApplicationLauncher.register();
        SystemConfigurationProxy.register();
        NullKeychain.register();
        LoginControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new LoginControllerFactory() {
            @Override
            protected LoginController create(final Controller c) {
                return create();
            }

            @Override
            protected LoginController create(final Session s) {
                return create();
            }

            @Override
            protected LoginController create() {
                return new AbstractLoginController() {
                    @Override
                    public void warn(final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
                        //
                    }

                    @Override
                    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                        throw new LoginCanceledException();
                    }
                };
            }
        });
    }

    @BeforeClass
    public static void register() {
        PathFactory.register(new NullProtocol(), new PathFactory() {
            @Override
            protected Path create(final Session session, final String path, final int type) {
                return new NullPath(path, type) {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }

            @Override
            protected Path create(final Session session, final String parent, final String name, final int type) {
                return new NullPath(parent + name, type) {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }

            @Override
            protected Path create(final Session session, final String parent, final Local file) {
                return new NullPath(parent + file.getName(), file.attributes().getType()) {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }

            @Override
            protected Path create(final Session session, final Object dict) {
                return null;
            }
        });
    }

    @Before
    public void preferences() {
        Preferences.instance().setProperty("application.support.path", System.getProperty("java.io.tmpdir"));
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