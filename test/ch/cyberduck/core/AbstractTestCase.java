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

import ch.cyberduck.core.date.DefaultUserDateFormatter;
import ch.cyberduck.core.local.DefaultLocalTrashFeature;
import ch.cyberduck.core.local.DisabledApplicationBadgeLabeler;
import ch.cyberduck.core.local.DisabledApplicationLauncher;
import ch.cyberduck.core.local.DisabledIconService;
import ch.cyberduck.core.local.DisabledQuarantineService;
import ch.cyberduck.core.local.NullApplicationFinder;
import ch.cyberduck.core.local.NullFileDescriptor;
import ch.cyberduck.core.local.NullLocalSymlinkFeature;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.ui.growl.DisabledNotificationService;
import ch.cyberduck.ui.resources.DisabledIconCache;

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
        PreferencesFactory.set(new TestPreferences());
        ProtocolFactory.register();
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

    private static class TestPreferences extends MemoryPreferences {
        @Override
        protected void setFactories() {
            super.setFactories();

            defaults.put("factory.local.class", Local.class.getName());
            defaults.put("factory.supportdirectoryfinder.class", TemporarySupportDirectoryFinder.class.getName());

            defaults.put("factory.passwordstore.class", DisabledPasswordStore.class.getName());
            defaults.put("factory.certificatestore.class", DisabledCertificateStore.class.getName());
            defaults.put("factory.logincallback.class", DisabledLoginCallback.class.getName());
            defaults.put("factory.hostkeycallback.class", DisabledHostKeyCallback.class.getName());
            defaults.put("factory.proxy.class", DisabledProxyFinder.class.getName());
            defaults.put("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());

            defaults.put("factory.locale.class", DisabledLocale.class.getName());
            defaults.put("factory.iconcache.class", DisabledIconCache.class.getName());

            defaults.put("factory.serializer.class", PlistSerializer.class.getName());
            defaults.put("factory.deserializer.class", PlistDeserializer.class.getName());
            defaults.put("factory.reader.profile.class", ProfilePlistReader.class.getName());
            defaults.put("factory.writer.profile.class", PlistWriter.class.getName());
            defaults.put("factory.reader.transfer.class", TransferPlistReader.class.getName());
            defaults.put("factory.writer.transfer.class", PlistWriter.class.getName());
            defaults.put("factory.reader.host.class", HostPlistReader.class.getName());
            defaults.put("factory.writer.host.class", PlistWriter.class.getName());

            defaults.put("factory.dateformatter.class", DefaultUserDateFormatter.class.getName());
            defaults.put("factory.rendezvous.class", DisabledRendezvous.class.getName());
            defaults.put("factory.trash.class", DefaultLocalTrashFeature.class.getName());
            defaults.put("factory.quarantine.class", DisabledQuarantineService.class.getName());
            defaults.put("factory.symlink.class", NullLocalSymlinkFeature.class.getName());

            defaults.put("factory.terminalservice.class", DisabledTerminalService.class.getName());
            defaults.put("factory.applicationfinder.class", NullApplicationFinder.class.getName());
            defaults.put("factory.applicationlauncher.class", DisabledApplicationLauncher.class.getName());
            defaults.put("factory.badgelabeler.class", DisabledApplicationBadgeLabeler.class.getName());
            defaults.put("factory.filedescriptor.class", NullFileDescriptor.class.getName());
            defaults.put("factory.iconservice.class", DisabledIconService.class.getName());
            defaults.put("factory.notification.class", DisabledNotificationService.class.getName());
            defaults.put("factory.schemehandler.class", DisabledSchemeHandler.class.getName());
        }

        @Override
        protected void setDefaults() {
            super.setDefaults();

            final Local settings = new TemporarySupportDirectoryFinder().find();

            defaults.put("application.support.path", settings.getAbsolute());
            defaults.put("application.profiles.path", settings.getAbsolute());
            defaults.put("application.receipt.path", settings.getAbsolute());
            defaults.put("application.bookmarks.path", settings.getAbsolute());
            defaults.put("queue.download.folder", settings.getAbsolute());
        }
    }
}