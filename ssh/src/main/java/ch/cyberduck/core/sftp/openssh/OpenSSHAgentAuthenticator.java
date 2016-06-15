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

import ch.cyberduck.core.sftp.AgentAuthenticator;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class OpenSSHAgentAuthenticator extends AgentAuthenticator {
    private static final Logger log = Logger.getLogger(OpenSSHAgentAuthenticator.class);

    private AgentProxy proxy;

    public OpenSSHAgentAuthenticator() {
        try {
            proxy = new AgentProxy(new SSHAgentConnector(new JNAUSocketFactory()));
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
            log.warn(String.format("Disabled agent %s", this));
            return Collections.emptyList();
        }
        if(null == proxy) {
            return Collections.emptyList();
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Retrieve identities from proxy %s", proxy));
        }
        final List<Identity> identities
                = new ArrayList<Identity>();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Found %d identities", identities.size()));
        }
        Collections.addAll(identities, proxy.getIdentities());
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
