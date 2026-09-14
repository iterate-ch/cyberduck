package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Factory;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

public class OpenSSHProxyCommandConnectorTest {

    @Test
    public void testSubstitute() {
        assertEquals("ssh -W internal.example.org:22 bastion.example.org",
                OpenSSHProxyCommandConnector.substitute("ssh -W %h:%p bastion.example.org", "internal.example.org", 22, "jenkins"));
        assertEquals("connect -H proxy:8080 internal.example.org 2222",
                OpenSSHProxyCommandConnector.substitute("connect -H proxy:8080 %h %p", "internal.example.org", 2222, null));
        assertEquals("aws ssm start-session --target i-0123 --document-name AWS-StartSSHSession --parameters portNumber=22",
                OpenSSHProxyCommandConnector.substitute("aws ssm start-session --target i-0123 --document-name AWS-StartSSHSession --parameters portNumber=%p", "i-0123", 22, "ec2-user"));
    }

    @Test
    public void testSubstituteRemoteUser() {
        assertEquals("cloudflared access ssh --hostname internal.example.org --user fred",
                OpenSSHProxyCommandConnector.substitute("cloudflared access ssh --hostname %h --user %r", "internal.example.org", 22, "fred"));
    }

    @Test
    public void testSubstitutePercentLiteral() {
        assertEquals("echo 100% >&2; nc host 22",
                OpenSSHProxyCommandConnector.substitute("echo 100%% >&2; nc %h %p", "host", 22, "u"));
    }

    @Test
    public void testConnectReadsProcessOutput() throws IOException {
        assumeFalse(Factory.Platform.getDefault().equals(Factory.Platform.Name.windows));
        final OpenSSHProxyCommandConnector connector = new OpenSSHProxyCommandConnector();
        try {
            connector.connect("printf 'SSH-2.0-test'", "localhost", 22, "test");
            final byte[] buffer = new byte[12];
            int offset = 0;
            int read;
            while(offset < buffer.length && (read = connector.getInputStream().read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
            }
            assertArrayEquals("SSH-2.0-test".getBytes(), buffer);
        }
        finally {
            connector.close();
        }
    }
}
