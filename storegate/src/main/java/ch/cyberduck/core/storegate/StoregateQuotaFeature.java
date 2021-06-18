package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.AccountSettingsApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.AccountStorage;

public class StoregateQuotaFeature implements Quota {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateQuotaFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final AccountSettingsApi account = new AccountSettingsApi(session.getClient());
            final AccountStorage quota = account.accountSettingsGetAccountStorage();
            return new Space(quota.getUsed(), quota.getAvailable());
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Failure to read attributes of {0}", e,
                new DefaultHomeFinderService(session).find());
        }
    }
}
