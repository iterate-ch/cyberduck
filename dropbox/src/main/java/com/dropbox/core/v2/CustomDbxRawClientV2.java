package com.dropbox.core.v2;

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

import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.common.PathRoot;

public final class CustomDbxRawClientV2 extends DbxRawClientV2 {
    /**
     * @param requestConfig Configuration controlling How requests should be issued to Dropbox servers.
     * @param host          Dropbox server hostnames (primarily for internal use)
     * @param userId        The user ID of the current Dropbox account. Used for multi-Dropbox account use-case.
     * @param pathRoot      We will send this value in Dropbox-API-Path-Root header if it presents.
     */
    public CustomDbxRawClientV2(final DbxRequestConfig requestConfig, final DbxHost host, final String userId, final PathRoot pathRoot) {
        super(requestConfig, host, userId, pathRoot);
    }

    @Override
    protected void addAuthHeaders(final List<HttpRequestor.Header> headers) {
        // OAuth Bearer added in interceptor
    }

    @Override
    public DbxRefreshResult refreshAccessToken() throws DbxException {
        throw new DbxException("Ignore");
    }

    @Override
    protected boolean canRefreshAccessToken() {
        return false;
    }

    @Override
    protected boolean needsRefreshAccessToken() {
        return false;
    }

    @Override
    public CustomDbxRawClientV2 withPathRoot(final PathRoot root) {
        if(null == root) {
            return this;
        }
        return new CustomDbxRawClientV2(
                this.getRequestConfig(),
                this.getHost(),
                this.getUserId(),
                root
        );
    }
}
