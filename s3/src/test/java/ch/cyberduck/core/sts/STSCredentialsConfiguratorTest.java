package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class STSCredentialsConfiguratorTest {

    @Mock
    private Appender mockedAppender;

    @Captor
    ArgumentCaptor<LogEvent> loggingEventCaptor;

    @Test
    public void testConfigure() throws Exception {
        new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol()));
    }

    @Test
    @Ignore
    public void readFailureForInvalidAWSCredentialsProfileEntry() throws Exception {
/*
        PreferencesFactory.get().setProperty("local.user.home", new File("src/test/resources/invalid").getAbsolutePath());
        Logger root = Logger.getRootLogger();
        root.addAppender(mockedAppender);
        root.setLevel(Level.WARN);
        new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol()));
        verify(mockedAppender, Mockito.atLeast(1)).doAppend(loggingEventCaptor.capture());
        assertTrue(loggingEventCaptor.getAllValues().get(0).getMessage().toString().contains("Failure reading Local"));
*/
    }

    @Test
    @Ignore
    public void readSuccessForValidAWSCredentialsProfileEntry() throws Exception {
/*
        PreferencesFactory.get().setProperty("local.user.home", new File("src/test/resources/valid").getAbsolutePath());
        Logger root = Logger.getRootLogger();
        root.addAppender(mockedAppender);
        root.setLevel(Level.WARN);
        new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol()));
        verify(mockedAppender, Mockito.atLeast(0)).doAppend(loggingEventCaptor.capture());
*/
    }
}
