package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerData;

import org.apache.commons.lang3.StringUtils;

public class SDSQuotaFeature implements Quota {

    private final SDSSession session;

    public SDSQuotaFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final CustomerData info = new UserApi(session.getClient()).getCustomerInfo(StringUtils.EMPTY, null);
            return new Space(info.getSpaceUsed(), info.getSpaceLimit() - info.getSpaceUsed());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Failure reading quota information", e);
        }
    }
}
