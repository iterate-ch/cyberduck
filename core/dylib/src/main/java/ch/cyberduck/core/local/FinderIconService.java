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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferStatus;

public class FinderIconService implements IconService {

    private final IconService[] delegates;

    public FinderIconService() {
        this(new WorkspaceIconService(), new FoundationProgressIconService());
    }

    public FinderIconService(final IconService... delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean set(final Local file, final String image) {
        for(IconService delegate : delegates) {
            delegate.set(file, image);
        }
        return true;
    }

    @Override
    public boolean set(final Local file, final TransferStatus progress) {
        for(IconService delegate : delegates) {
            delegate.set(file, progress);
        }
        return true;
    }

    @Override
    public boolean remove(final Local file) {
        for(IconService delegate : delegates) {
            delegate.remove(file);
        }
        return true;
    }
}
