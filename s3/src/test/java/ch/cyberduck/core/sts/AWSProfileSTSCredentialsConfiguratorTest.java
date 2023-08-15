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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class AWSProfileSTSCredentialsConfiguratorTest {

    @Test
    public void testConfigure() throws Exception {
        new AWSProfileSTSCredentialsConfigurator(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol()));
    }

    @Test
    public void readFailureForInvalidAWSCredentialsProfileEntry() throws Exception {
        final Credentials credentials = new Credentials("test_s3_profile");
        final Credentials verify = new AWSProfileSTSCredentialsConfigurator(LocalFactory.get(new File("src/test/resources/invalid/.aws").getAbsolutePath()),
                new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol(), StringUtils.EMPTY, credentials));
        assertEquals(credentials, verify);
    }

    @Test
    public void readSuccessForValidAWSCredentialsProfileEntry() throws Exception {
        final Credentials verify = new AWSProfileSTSCredentialsConfigurator(LocalFactory.get(new File("src/test/resources/valid/.aws").getAbsolutePath())
                , new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledPasswordCallback())
                .reload().configure(new Host(new TestProtocol(), StringUtils.EMPTY, new Credentials("test_s3_profile")));
        assertEquals("EXAMPLEKEYID", verify.getUsername());
        assertEquals("EXAMPLESECRETKEY", verify.getPassword());
        assertEquals("EXAMPLETOKEN", verify.getToken());
    }
}
