package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.sshj.AuthAgent;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

public class SFTPAgentAuthentication implements AuthenticationProvider {
    private static final Logger log = Logger.getLogger(SFTPAgentAuthentication.class);

    private final SFTPSession session;
    private final AgentAuthenticator agent;

    public SFTPAgentAuthentication(final SFTPSession session, final AgentAuthenticator agent) {
        this.session = session;
        this.agent = agent;
    }

    @Override
    public boolean authenticate(final Host bookmark, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel)
        throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using agent %s for %s", agent, bookmark));
        }
        for(Identity identity : agent.getIdentities()) {
            try {
                session.getClient().auth(bookmark.getCredentials().getUsername(), new AuthAgent(agent.getProxy(), identity));
                // Successfully authenticated
                break;
            }
            catch(UserAuthException e) {
                cancel.verify();
                // Continue;
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
