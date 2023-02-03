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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class STSCredentialsConfiguratorTest {

    @Test
    public void testConfigure() throws Exception {
        new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol()));
    }

    @Test
    public void readFailureForInvalidAWSCredentialsProfileEntry() throws Exception {
        PreferencesFactory.get().setProperty("local.user.home", new File("src/test/resources/invalid").getAbsolutePath());
        final Credentials credentials = new Credentials("test_s3_profile");
        final Credentials verify = new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol(), StringUtils.EMPTY, credentials));
        assertEquals(credentials, verify);
    }

    @Test
    public void readSuccessForValidAWSCredentialsProfileEntry() throws Exception {
        PreferencesFactory.get().setProperty("local.user.home", new File("src/test/resources/valid").getAbsolutePath());
        final Credentials verify = new STSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback()).configure(new Host(new TestProtocol(), StringUtils.EMPTY, new Credentials("test_s3_profile")));
        assertEquals("EXAMPLEKEYID", verify.getUsername());
        assertEquals("EXAMPLESECRETKEY", verify.getPassword());
        assertEquals("EXAMPLETOKEN", verify.getToken());
    }
}
