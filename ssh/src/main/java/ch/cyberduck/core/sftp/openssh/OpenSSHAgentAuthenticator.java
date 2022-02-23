package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.sftp.auth.AgentAuthenticator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

// Implements a wrapper around RandomAccessFile for use with jsch's
// SSH connector to support Windows' OpenSSH fork.
class RandomAccessFileSocketFactory implements USocketFactory
{
    class WindowsSocket extends Socket
    {
        private RandomAccessFile raf;
        WindowsSocket(String path) throws IOException
        {
            raf = new RandomAccessFile(path, "rw");
        }

        public int readFull(byte[] buf, int s, int len) throws IOException
        {
            try {
                raf.readFully(buf, s, len);
            } catch (EOFException e) {
                return -1;
            }
            return len;
        }
        public void write(byte[] buf, int s, int len) throws IOException
        {
            raf.write(buf, s, len);
        }
        public void close() throws IOException
        {
            raf.close();
        }
    }

    public Socket open(String path) throws IOException
    {
        return new WindowsSocket(path);
    }
}

public class OpenSSHAgentAuthenticator extends AgentAuthenticator {
    private static final Logger log = LogManager.getLogger(OpenSSHAgentAuthenticator.class);

    private AgentProxy proxy;

    public OpenSSHAgentAuthenticator(final String socket, final boolean windows) {
        try {
            if (windows) {
                proxy = new AgentProxy(new SSHAgentConnector(new RandomAccessFileSocketFactory(), "\\\\.\\pipe\\openssh-ssh-agent"));
            } else {
                proxy = new AgentProxy(new SSHAgentConnector(new JNAUSocketFactory(), socket));
            }
        }
        catch(AgentProxyException e) {
            log.warn(String.format("Agent proxy %s failed with %s", this, e));
        }
    }

    @Override
    public AgentProxy getProxy() {
        return proxy;
    }

    @Override
    public Collection<Identity> getIdentities() {
        if(!SSHAgentConnector.isConnectorAvailable()) {
            log.warn(String.format("SSH agent %s is not running", this));
            return Collections.emptyList();
        }
        if(null == proxy) {
            return Collections.emptyList();
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Retrieve identities from proxy %s", proxy));
        }
        final List<Identity> identities = Arrays.asList(proxy.getIdentities());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Found %d identities", identities.size()));
        }
        return identities;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSSHAgentAuthenticator{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
