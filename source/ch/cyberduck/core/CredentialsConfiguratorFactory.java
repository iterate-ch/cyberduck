package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.sftp.OpenSSHCredentialsConfigurator;

/**
 * @version $Id:$
 */
public final class CredentialsConfiguratorFactory {

    private CredentialsConfiguratorFactory() {
        //
    }

    /**
     * @param protocol Protocol
     * @return Configurator for default settings
     */
    public static CredentialsConfigurator get(final Protocol protocol) {
        if(protocol.equals(Protocol.SFTP)) {
            return new OpenSSHCredentialsConfigurator();
        }
        return new NullCredentialsConfigurator();
    }

    private static final class NullCredentialsConfigurator implements CredentialsConfigurator {
        @Override
        public void configure(Credentials credentials, String hostname) {
            //
        }
    }
}