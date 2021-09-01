package ch.cyberduck.core.owncloud;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.nextcloud.NextcloudShareProvider;
import ch.cyberduck.core.nextcloud.NextcloudUrlProvider;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

public class OwncloudSession extends DAVSession {

    public OwncloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Lock.class) {
            // https://github.com/nextcloud/server/issues/1308
            return null;
        }
        if(type == Write.class) {
            return (T) new NextcloudWriteFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new NextcloudUrlProvider(this);
        }
        if(type == PromptUrlProvider.class) {
            return (T) new NextcloudShareProvider(this);
        }
        return super._getFeature(type);
    }
}
