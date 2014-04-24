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

import ch.ethz.ssh2.auth.AgentIdentity;
import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.Buffer;
import com.jcraft.jsch.agentproxy.Identity;

/**
 * @version $Id$
 */
public abstract class AgentAuthenticator implements ch.ethz.ssh2.auth.AgentProxy {

    protected final class WrappedAgentIdentity implements AgentIdentity {
        AgentProxy proxy;
        Identity identity;
        String algorithm;

        public WrappedAgentIdentity(final AgentProxy proxy, final Identity identity) {
            this.proxy = proxy;
            this.identity = identity;
            this.algorithm = new String((new Buffer(identity.getBlob())).getString());
        }

        public String getAlgName() {
            return algorithm;
        }

        public byte[] getPublicKeyBlob() {
            return identity.getBlob();
        }

        public byte[] sign(byte[] bytes) {
            return proxy.sign(getPublicKeyBlob(), bytes);
        }
    }
}
