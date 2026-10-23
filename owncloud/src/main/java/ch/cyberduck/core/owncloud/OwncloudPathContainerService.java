package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.ocs.OcsCapabilities;

/**
 * Root folder of personal space, project space or share is container
 */
public class OwncloudPathContainerService extends DefaultPathContainerService {

    private final OcsCapabilities ocs;

    public OwncloudPathContainerService(final OcsCapabilities ocs) {
        this.ocs = ocs;
    }

    /**
     * @return True for root and folders listing spaces or shares. Always false when spaces are not supported
     */
    public boolean isVirtual(final Path file) {
        if(ocs.spaces) {
            return file.isRoot()
                    || new SimplePathPredicate(OwncloudPathMapper.SPACES_FOLDER).test(file)
                    || new SimplePathPredicate(OwncloudPathMapper.SHARES_FOLDER).test(file);
        }
        return false;
    }

    @Override
    public boolean isContainer(final Path file) {
        if(!ocs.spaces) {
            return false;
        }
        if(file.isRoot()) {
            return false;
        }
        if(new SimplePathPredicate(OwncloudPathMapper.PERSONAL_FOLDER).test(file)) {
            return true;
        }
        return new SimplePathPredicate(OwncloudPathMapper.SPACES_FOLDER).test(file.getParent())
                || new SimplePathPredicate(OwncloudPathMapper.SHARES_FOLDER).test(file.getParent());
    }
}
