package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import java.io.IOException;

import com.github.sardine.DavQuota;
import com.github.sardine.impl.SardineException;

public class DAVQuotaFeature implements Quota {

    private final DAVSession session;

    public DAVQuotaFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new DefaultHomeFinderService(session).find();
        try {
            final DavQuota quota = session.getClient().getQuota(new DAVPathEncoder().encode(home));
            return new Space(
                    quota.getQuotaUsedBytes() > 0 ? quota.getQuotaUsedBytes() : 0,
                    quota.getQuotaAvailableBytes() > 0 ? quota.getQuotaAvailableBytes() : Long.MAX_VALUE
            );
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, home);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, home);
        }
    }
}
