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
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;

public class WindowsOpenSSHAgentAuthenticator extends OpenSSHAgentAuthenticator {
    private final static String SSH_AGENT_PIPE = "\\\\.\\pipe\\openssh-ssh-agent";

    public WindowsOpenSSHAgentAuthenticator(final String socketPath) throws AgentProxyException {
        super(new AgentProxy(new SSHAgentConnector(new RandomAccessFileSocketFactory(), fixupSocketPath(socketPath))));
    }

    static String fixupSocketPath(final String path) {
        if(path == null) {
            return SSH_AGENT_PIPE;
        }
        return path;
    }

    /**
     * Implements a wrapper around RandomAccessFile for use with jsch's SSH connector to support Windows' OpenSSH fork.
     */
    private static class RandomAccessFileSocketFactory implements USocketFactory {
        public Socket open(String path) throws IOException {
            return new WindowsSocket(path);
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
