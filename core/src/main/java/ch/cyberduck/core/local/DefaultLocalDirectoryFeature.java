package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Directory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class DefaultLocalDirectoryFeature implements Directory {
    private static final Logger log = LogManager.getLogger(DefaultLocalDirectoryFeature.class);

    @Override
    public void mkdir(final Local file) throws AccessDeniedException {
        try {
            Files.createDirectories(Paths.get(file.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                "Cannot create folder {0}", "Error"), file.getName()), e);
        }
    }
}
