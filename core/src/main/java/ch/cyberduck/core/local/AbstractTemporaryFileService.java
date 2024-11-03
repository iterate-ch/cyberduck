package ch.cyberduck.core.local;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractTemporaryFileService implements TemporaryFileService {
    private static final Logger log = LogManager.getLogger(AbstractTemporaryFileService.class);

    private final Local temp;

    public AbstractTemporaryFileService(final Local temp) {
        this.temp = temp;
    }

    /**
     * Set of filenames to be deleted on VM exit through a shutdown hook.
     */
    private final Set<Local> files = new CopyOnWriteArraySet<>();

    /**
     * Delete on exit
     *
     * @param file File reference
     */
    protected Local delete(final Local file) {
        files.add(file);
        return file;
    }

    protected String shorten(final String path, int limit) {
        if(path.length() > limit) {
            return DigestUtils.md5Hex(path);
        }
        return path;
    }

    @Override
    public void shutdown() {
        final List<Local> list = new ArrayList<>(files);
        Collections.reverse(list);
        for(Local f : list) {
            try {
                f.delete();
            }
            catch(AccessDeniedException | NotfoundException e) {
                log.warn("Failure deleting file {} in shutdown hook. {}", f, e.getMessage());
            }
        }
    }

    protected Local create(final Local folder, final String filename) {
        try {
            log.debug("Creating intermediate folder {}", folder);
            folder.mkdir();
        }
        catch(AccessDeniedException e) {
            log.warn("Failure {} creating intermediate folder", e);
            return this.delete(LocalFactory.get(temp,
                    String.format("%s-%s", new AlphanumericRandomStringService().random(), filename)));
        }
        this.delete(folder);
        return this.delete(LocalFactory.get(folder, filename));
    }
}
