package ch.cyberduck.core.auth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AWSSessionCredentialsRetrieverTest {

    @Test
    public void testParse() throws Exception {
        final Credentials c = new AWSSessionCredentialsRetriever(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new DisabledTranscriptListener(),
            "http://169.254.169.254/latest/meta-data/iam/security-credentials/s3access")
            .parse(IOUtils.toInputStream("{\n" +
                "  \"Code\" : \"Success\",\n" +
                "  \"LastUpdated\" : \"2012-04-26T16:39:16Z\",\n" +
                "  \"Type\" : \"AWS-HMAC\",\n" +
                "  \"AccessKeyId\" : \"AKIAIOSFODNN7EXAMPLE\",\n" +
                "  \"SecretAccessKey\" : \"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\",\n" +
                "  \"Token\" : \"token\",\n" +
                "  \"Expiration\" : \"2012-04-27T22:39:16Z\"\n" +
                "}", Charset.defaultCharset()));
        assertEquals("AKIAIOSFODNN7EXAMPLE", c.getTokens().getAccessKeyId());
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", c.getTokens().getSecretAccessKey());
        assertEquals("token", c.getTokens().getSessionToken());
        assertEquals(1335566356000L, c.getTokens().getExpiryInMilliseconds(), 0L);
    }

    @Test(expected = ConnectionTimeoutException.class)
    @Ignore
    public void testGet() throws Exception {
        new AWSSessionCredentialsRetriever(new DisabledX509TrustManager(), new DefaultX509KeyManager(), new ProtocolFactory(Collections.singleton(new DAVProtocol())), new DisabledTranscriptListener(),
            "http://169.254.169.254/latest/meta-data/iam/security-credentials/s3access")
            .get();
    }
}
