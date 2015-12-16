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

import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Locale;
import java.util.Properties;

/**
 * @version $Id$
 */
public class AbstractTestCase {

    protected static Properties properties = System.getProperties();

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        BasicConfigurator.configure();
    }

    private ActionOperationBatcher pool;

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
    public static void setup() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        PreferencesFactory.set(new MemoryPreferences());
    }

    @BeforeClass
    public static void protocols() throws Exception {
        ProtocolFactory.register(new TestProtocol());
    }

    @Before
    public void pool() {
        pool = ActionOperationBatcherFactory.get();
    }

    @Before
    public void preferences() {
        Logger.getRootLogger().setLevel(Level.WARN);
        Logger.getLogger(AbstractTestCase.class.getPackage().getName()).setLevel(Level.DEBUG);
    }

    @After
    public void post() {
        pool.operate();
    }
}