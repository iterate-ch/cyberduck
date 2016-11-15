/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.DisabledUrlProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.shared.DisabledMoveFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.jets3t.service.Jets3tProperties;

public class SpectraSession extends S3Session {

    private final SpectraBulkService bulk = new SpectraBulkService(this);

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, trust, key, proxy);
    }

    @Override
    protected Jets3tProperties configure() {
        final Jets3tProperties configuration = super.configure();
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
        configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        return configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == Bulk.class) {
            return (T) bulk;
        }
        if(type == Touch.class) {
            return (T) new SpectraTouchFeature(this);
        }
        if(type == Directory.class) {
            return (T) new SpectraDirectoryFeature(this);
        }
        if(type == Move.class) {
            return (T) new DisabledMoveFeature();
        }
        if(type == AclPermission.class) {
            return null;
        }
        if(type == Versioning.class) {
            return null;
        }
        if(type == UrlProvider.class) {
            return (T) new DisabledUrlProvider();
        }
        if(type == Delete.class) {
            return (T) new SpectraDeleteFeature(this);
        }
        if(type == Copy.class) {
            // Disable copy operation not supported
            return null;
        }
        if(type == Write.class) {
            return (T) new SpectraWriteFeature(this);
        }
        if(type == Read.class) {
            return (T) new SpectraReadFeature(this, bulk);
        }
        if(type == Upload.class) {
            return (T) new SpectraUploadFeature(new SpectraWriteFeature(this), bulk);
        }
        if(type == Download.class) {
            return (T) new DefaultDownloadFeature(new SpectraReadFeature(this, bulk));
        }
        if(type == Headers.class) {
            return null;
        }
        if(type == DistributionConfiguration.class) {
            return null;
        }
        return super.getFeature(type);
    }
}
