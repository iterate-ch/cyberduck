package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.sshj.AuthAgent;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

public class SFTPAgentAuthentication implements SFTPAuthentication {
    private static final Logger log = Logger.getLogger(SFTPAgentAuthentication.class);

    private SFTPSession session;

    private AgentAuthenticator agent;

    public SFTPAgentAuthentication(final SFTPSession session, final AgentAuthenticator agent) {
        this.session = session;
        this.agent = agent;
    }

    @Override
    public boolean authenticate(final Host host, final LoginCallback controller, final CancelCallback cancel)
            throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using agent %s with credentials %s", agent, host.getCredentials()));
        }
        for(Identity identity : agent.getIdentities()) {
            try {
                session.getClient().auth(host.getCredentials().getUsername(), new AuthAgent(agent.getProxy(), identity));
                // Successfully authenticated
                break;
            }
            catch(UserAuthException e) {
                cancel.verify();
                // continue;
            }
            catch(Buffer.BufferException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
            catch(TransportException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
        }
        return session.getClient().isAuthenticated();
    }
}
