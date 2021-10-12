package ch.cyberduck.core.gmxcloud;

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
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.UserInfoApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UserInfoResponseModel;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

public class GmxcloudQuotaFeature implements Quota {

    private final GmxcloudSession session;

    public GmxcloudQuotaFeature(final GmxcloudSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final GmxcloudApiClient gmxcloudApiClient = new GmxcloudApiClient(session);
            final UserInfoApi userInfoApi = new UserInfoApi(gmxcloudApiClient);
            final UserInfoResponseModel userInfoResponseModel = userInfoApi.userinfoGet(null, null);
            return new Space(userInfoResponseModel.getQuotas().getContentSize().getCurrent().longValue(), userInfoResponseModel.getQuotas().getContentSize().getMax());
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Failure to read attributes of {0}", e,
                new DefaultHomeFinderService(session).find());
        }
    }
}
