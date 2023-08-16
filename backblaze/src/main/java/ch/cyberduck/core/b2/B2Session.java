package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import synapticloop.b2.B2ApiClient;
import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2AuthorizeAccountResponse;

public class B2Session extends HttpSession<B2ApiClient> {
    private static final Logger log = LogManager.getLogger(B2Session.class);

    private B2ErrorResponseInterceptor retryHandler;

    private final B2VersionIdProvider fileid = new B2VersionIdProvider(this);
    private final B2ListService listService = new B2ListService(this, fileid);

    public B2Session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected B2ApiClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setServiceUnavailableRetryStrategy(retryHandler = new B2ErrorResponseInterceptor(
                this, fileid));
        configuration.addInterceptorLast(retryHandler);
        return new B2ApiClient(configuration.build());
    }

    @Override
    public void logout() throws BackgroundException {
        try {
            client.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            fileid.clear();
        }
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final String accountId = host.getCredentials().getUsername();
            final String applicationKey = host.getCredentials().getPassword();
            // Save tokens for 401 error response when expired
            final B2AuthorizeAccountResponse response = client.authenticate(accountId, applicationKey);
            // When present, access is restricted to one bucket
            if(StringUtils.isNotBlank(response.getBucketId())) {
                final PathAttributes attributes = new PathAttributes();
                attributes.setVersionId(response.getBucketId());
                listService.withBucket(new Path(PathNormalizer.normalize(response.getBucketName()), EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
            }
            retryHandler.setTokens(accountId, applicationKey, response.getAuthorizationToken());
            if(new HostPreferences(host).getBoolean("b2.upload.largeobject.auto")) {
                final int recommendedPartSize = response.getRecommendedPartSize();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Set large upload part size to %d", recommendedPartSize));
                }
                host.setProperty("b2.upload.largeobject.size", String.valueOf(recommendedPartSize));
                host.setProperty("b2.copy.largeobject.size", String.valueOf(recommendedPartSize));
                final int absoluteMinimumPartSize = response.getAbsoluteMinimumPartSize();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Set large upload minimum part size to %d", absoluteMinimumPartSize));
                }
                host.setProperty("b2.upload.largeobject.size.minimum", String.valueOf(absoluteMinimumPartSize));
            }
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) listService;
        }
        if(type == Touch.class) {
            return (T) new B2TouchFeature(this, fileid);
        }
        if(type == Read.class) {
            return (T) new B2ReadFeature(this, fileid);
        }
        if(type == Upload.class) {
            return (T) new B2ThresholdUploadService(this, fileid);
        }
        if(type == MultipartWrite.class) {
            return (T) new B2LargeUploadWriteFeature(this, fileid);
        }
        if(type == Write.class) {
            return (T) new B2WriteFeature(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new B2DirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new B2DeleteFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new B2ThresholdCopyFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new B2MoveFeature(this, fileid);
        }
        if(type == UrlProvider.class) {
            return (T) new B2UrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new B2AuthorizedUrlProvider(this, fileid);
        }
        if(type == Find.class) {
            return (T) new B2FindFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new B2AttributesFinderFeature(this, fileid);
        }
        if(type == AclPermission.class) {
            return (T) new B2BucketTypeFeature(this, fileid);
        }
        if(type == Location.class) {
            return (T) new B2BucketTypeFeature(this, fileid);
        }
        if(type == VersionIdProvider.class) {
            return (T) fileid;
        }
        if(type == Lifecycle.class) {
            return (T) new B2LifecycleFeature(this, fileid);
        }
        if(type == Search.class) {
            return (T) new B2SearchFeature(this, fileid);
        }
        if(type == Headers.class) {
            return (T) new B2MetadataFeature(this, fileid);
        }
        if(type == Metadata.class) {
            return (T) new B2MetadataFeature(this, fileid);
        }
        if(type == Versioning.class) {
            return (T) new B2VersioningFeature(this, fileid);
        }
        return super._getFeature(type);
    }
}
