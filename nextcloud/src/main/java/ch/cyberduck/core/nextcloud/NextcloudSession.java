package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

public class NextcloudSession extends DAVSession {

    public NextcloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host), new NextcloudHomeFeature(host));
        }
        if(type == ListService.class) {
            return (T) new NextcloudListService(this);
        }
        if(type == Directory.class) {
            return (T) new DAVDirectoryFeature(this, new NextcloudAttributesFinderFeature(this));
        }
        if(type == Touch.class) {
            return (T) new DAVTouchFeature(new NextcloudWriteFeature(this), new NextcloudAttributesFinderFeature(this));
        }
        if(type == AttributesFinder.class) {
            return (T) new NextcloudAttributesFinderFeature(this);
        }
        if(type == Upload.class) {
            return (T) new HttpUploadFeature(new NextcloudWriteFeature(this));
        }
        if(type == Write.class) {
            return (T) new NextcloudWriteFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new NextcloudUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new NextcloudShareFeature(this);
        }
        if(type == Versioning.class) {
            return (T) new NextcloudVersioningFeature(this);
        }
        if(type == Delete.class) {
            return (T) new NextcloudDeleteFeature(this);
        }
        if(type == Read.class) {
            return (T) new NextcloudReadFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new NextcloudTimestampFeature(this);
        }
        return super._getFeature(type);
    }
}
