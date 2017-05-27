package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

/**
 * Created by tomascelaya on 5/26/17.
 */
public class MantaHomeFinderService extends DefaultHomeFinderService {

    private final MantaSession session;

    public MantaHomeFinderService(final MantaSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = super.find();
        if(home == DEFAULT_HOME) {
            return session.pathMapper.getNormalizedHomePath();
        }
        return home;
    }

    @Override
    public Path find(final Path root, final String path) {
        return super.find(root, path);
    }
}
