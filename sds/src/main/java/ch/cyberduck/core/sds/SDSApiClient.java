package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;

import org.apache.http.impl.client.CloseableHttpClient;

public class SDSApiClient extends ApiClient {

    private CloseableHttpClient client;

    public SDSApiClient(final CloseableHttpClient client) {
        this.client = client;
    }

    public CloseableHttpClient getClient() {
        return client;
    }
}
