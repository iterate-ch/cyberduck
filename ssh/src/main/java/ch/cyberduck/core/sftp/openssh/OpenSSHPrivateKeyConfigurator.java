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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;

public class OpenSSHPrivateKeyConfigurator {
    private static final Logger log = LogManager.getLogger(OpenSSHPrivateKeyConfigurator.class);

    private final Local directory;

    public OpenSSHPrivateKeyConfigurator() {
        this(LocalFactory.get(LocalFactory.get(), ".ssh"));
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

                @Override
                public Pattern toPattern() {
                    return Pattern.compile(".*\\.pub");
                }
            })) {
                final KeyFormat format;
                try {
                    format = KeyProviderUtil.detectKeyFileFormat(
                            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), true);
                }
                catch(AccessDeniedException | IOException e) {
                    log.debug("Ignore file {} with unknown format. {}", file, e.getMessage());
                    continue;
                }
                switch(format) {
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
            log.warn("Failure loading keys from directory {}", directory);
        }
        return keys;
    }
}
