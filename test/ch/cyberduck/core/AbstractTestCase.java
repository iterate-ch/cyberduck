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

import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.test.PlatformAwareClassRunner;
import ch.cyberduck.core.test.TestPreferences;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

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

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
@RunWith(PlatformAwareClassRunner.class)
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
        PreferencesFactory.set(new TestPreferences());
        ProtocolFactory.register();
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                new Local("profiles/Rackspace US.cyberduckprofile")
        );
        assertNotNull(profile);
        ProtocolFactory.register(profile);
    }

    @Before
    public void preferences() {
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
                    final ActionOperationBatcher p = ActionOperationBatcherFactory.get();
                    try {
                        return c.call();
                    }
                    finally {
                        p.operate();
                    }
                }
            });
        }
        for(int i = 0; i < repeat; i++) {
            queue.take().get();
        }
    }
}