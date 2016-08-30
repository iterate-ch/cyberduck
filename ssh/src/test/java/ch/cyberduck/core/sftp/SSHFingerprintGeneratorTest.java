package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.junit.Test;

import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;

import static org.junit.Assert.assertEquals;

public class SSHFingerprintGeneratorTest {


    @Test
    public void testFingerprint() throws Exception {
        final FileKeyProvider f = new OpenSSHKeyFile.Factory().create();
        f.init("", "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/71hmi4R+CZqGvZ+aVdaKIt5yb2H87yNAAcdtPAQBJBqKw/vR0iYeU/tnwKWRfnTK/NcN2H6yG/wx0o9WiavUhUaSUPesJo3/PpZ7fZMUk/Va8I7WI0i25XlWJTE8SMFftIuJ8/AVPNSCmL46qy93BlQb8W70O9XQD/yj/Cy6aPb9wlHxdaswrmdoIzI4BS28Tu1F45TalqarqTLm3wY4RpghxHo8LxCgNbmd0cr6XnOmz1RM+rlbkiuSdNphW3Ah2iCHMif/KdRCFCPi5LyUrdheOtQYvQCmFREczb3kyuQPCElQac4DeL37F9ZLLBHnRVi7KxFqDbcbNLadfExx dkocher@osaka.local");
        assertEquals("87:60:23:a3:56:b5:1a:24:8b:63:43:ea:5a:d4:e1:9d",
                new SSHFingerprintGenerator().fingerprint(f.getPublic())
        );
    }
}