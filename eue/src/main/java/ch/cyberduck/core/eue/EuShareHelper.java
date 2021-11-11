package ch.cyberduck.core.eue;/*
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
import ch.cyberduck.core.eue.io.swagger.client.api.GetUserSharesApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.UserSharesModel;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class EuShareHelper {

    private EuShareHelper() {
    }

    public static List<ShareCreationResponseEntity> getShareForResource(EueSession eueSession, String resourceId) throws ApiException {
        final GetUserSharesApi getUserSharesApi = new GetUserSharesApi(new EueApiClient(eueSession));
        final UserSharesModel sharesModel = getUserSharesApi.shareGet(null, null);
        return sharesModel.stream().filter(sm -> StringUtils.substringAfterLast(sm.getResourceURI(), "/").equals(resourceId)).collect(Collectors.toList());

    }

}
