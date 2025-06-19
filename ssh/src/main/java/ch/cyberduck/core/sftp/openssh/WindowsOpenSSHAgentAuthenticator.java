package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;

public class WindowsOpenSSHAgentAuthenticator extends OpenSSHAgentAuthenticator {
    public final static String SSH_AGENT_PIPE = "\\\\.\\pipe\\openssh-ssh-agent";

    public WindowsOpenSSHAgentAuthenticator() {
        this(SSH_AGENT_PIPE);
    }

    public WindowsOpenSSHAgentAuthenticator(final String socketPath) {
        super(new AgentProxy(new SSHAgentConnector(new RandomAccessFileSocketFactory(), socketPath)));
    }

    /**
     * Implements a wrapper around RandomAccessFile for use with jsch's SSH connector to support Windows' OpenSSH fork.
     */
    private static class RandomAccessFileSocketFactory implements USocketFactory {
        public Socket open(String path) throws IOException {
            try {
                return new WindowsSocket(path);
            }
            catch(Exception e) {
                if(e instanceof IOException) {
                    throw e;
                }

                // Wrap System.TimeoutException in IOException.
                throw new IOException(e);
            }
        }

        static class WindowsSocket extends Socket {
            private final RandomAccessFile raf;

            WindowsSocket(String path) throws IOException {
                raf = new RandomAccessFile(path, "rw");
            }

            public int readFull(byte[] buf, int s, int len) throws IOException {
                try {
                    raf.readFully(buf, s, len);
                }
                catch(EOFException e) {
                    return -1;
                }
                return len;
            }

            public void write(byte[] buf, int s, int len) throws IOException {
                raf.write(buf, s, len);
            }

            public void close() throws IOException {
                raf.close();
            }
        }
    }
}
