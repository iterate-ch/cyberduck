package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;

public class OpenSSHPrivateKeyConfigurator {
    private static final Logger log = Logger.getLogger(OpenSSHPrivateKeyConfigurator.class);

    public static final Local OPENSSH_CONFIGURATION_DIRECTORY
            = LocalFactory.get(Local.HOME, ".ssh");

    private final Local directory;

    public OpenSSHPrivateKeyConfigurator() {
        this(OPENSSH_CONFIGURATION_DIRECTORY);
    }

    public OpenSSHPrivateKeyConfigurator(final Local directory) {
        this.directory = directory;
    }

    public List<Local> list() {
        final List<Local> keys = new ArrayList<>();
        try {
            for(Local file : directory.list(new Filter<String>() {
                @Override
                public boolean accept(final String file) {
                    return !StringUtils.endsWith(file, ".pub");
                }
            })) {
                final KeyFormat format;
                try {
                    format = KeyProviderUtil.detectKeyFileFormat(
                            new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")), true);
                }
                catch(AccessDeniedException | IOException e) {
                    log.debug(String.format("Ignore file %s with unknown format. %s", file, e.getMessage()));
                    continue;
                }
                switch(format) {
                    case PKCS5:
                    case PKCS8:
                    case OpenSSH:
                    case OpenSSHv1:
                    case PuTTY:
                        keys.add(file);
                        break;
                }
            }
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure loading keys from directory %s", directory));
        }
        return keys;
    }
}
