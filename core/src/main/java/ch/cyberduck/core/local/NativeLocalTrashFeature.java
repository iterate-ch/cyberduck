package ch.cyberduck.core.local;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Trash;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import com.sun.jna.platform.FileUtils;


public class NativeLocalTrashFeature implements Trash {
    private static final Logger log = Logger.getLogger(NativeLocalTrashFeature.class);

    @Override
    public void trash(final Local file) throws LocalAccessDeniedException {
        try {
            FileUtils.getInstance().moveToTrash(new File[]{new File(file.getAbsolute())});
        }
        catch(IOException e) {
            log.warn(String.format("Failed to move %s to Trash", file.getName()));
            new DefaultLocalTrashFeature().trash(file);
        }
    }
}
