package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

public class OneDriveHomeFinderService extends DefaultHomeFinderService {

    public OneDriveHomeFinderService(final OneDriveSession session) {
        super(session.getHost());
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = super.find();
        if(home == DEFAULT_HOME) {
            return OneDriveListService.MYFILES_NAME;
        }
        return home;
    }
}
