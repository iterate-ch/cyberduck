package ch.cyberduck.core.sftp.auth;

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

import org.junit.Test;

import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.userauth.UserAuthException;

import static org.junit.Assert.assertEquals;

public class SFTPChallengeResponseInformationRequestTest {

    @Test
    public void testParseWithoutPrompts() throws Exception {
        final SSHPacket buf = new SSHPacket(Message.USERAUTH_60)
                .putString("")
                .putString("Open your Authenticator app, and enter the number '70' to sign in.")
                .putString("")
                .putUInt32(0);
        buf.readMessageID();
        final int rpos = buf.rpos();
        final SFTPChallengeResponseAuthentication.InformationRequest request = SFTPChallengeResponseAuthentication.InformationRequest.parse(buf);
        assertEquals("", request.name);
        assertEquals("Open your Authenticator app, and enter the number '70' to sign in.", request.instruction);
        assertEquals(0, request.prompts);
        // Buffer not consumed
        assertEquals(rpos, buf.rpos());
    }

    @Test
    public void testParseWithPrompt() throws Exception {
        final SSHPacket buf = new SSHPacket(Message.USERAUTH_60)
                .putString("n")
                .putString("")
                .putString("")
                .putUInt32(1)
                .putString("Entra Id Password: ")
                .putBoolean(false);
        buf.readMessageID();
        final int rpos = buf.rpos();
        final SFTPChallengeResponseAuthentication.InformationRequest request = SFTPChallengeResponseAuthentication.InformationRequest.parse(buf);
        assertEquals("n", request.name);
        assertEquals("", request.instruction);
        assertEquals(1, request.prompts);
        assertEquals(rpos, buf.rpos());
        assertEquals("n", buf.readString());
    }

    @Test(expected = UserAuthException.class)
    public void testParseTruncated() throws Exception {
        final SSHPacket buf = new SSHPacket(Message.USERAUTH_60).putString("n");
        buf.readMessageID();
        SFTPChallengeResponseAuthentication.InformationRequest.parse(buf);
    }
}
