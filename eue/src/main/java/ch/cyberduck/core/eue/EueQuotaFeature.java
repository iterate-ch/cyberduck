package ch.cyberduck.core.eue;

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

import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.UserInfoApi;
import ch.cyberduck.core.eue.io.swagger.client.model.UserInfoResponseModel;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EueQuotaFeature implements Quota {
    private static final Logger log = LogManager.getLogger(EueQuotaFeature.class);

    private final EueSession session;

    public EueQuotaFeature(final EueSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            final UserInfoApi userInfoApi = new UserInfoApi(client);
            final UserInfoResponseModel userInfoResponseModel = userInfoApi.userinfoGet(null, null);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received user info %s", userInfoResponseModel));
            }
            return new Space(userInfoResponseModel.getQuotas().getContentSize().getCurrent(),
                    userInfoResponseModel.getQuotas().getContentSize().getMax() - userInfoResponseModel.getQuotas().getContentSize().getCurrent());
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Failure to read attributes of {0}", e,
                new DefaultHomeFinderService(session).find());
        }
    }
}
