package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import com.microsoft.azure.storage.AuthenticationScheme;
import com.microsoft.azure.storage.Credentials;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ContainerListingDetails;

/**
 * @version $Id$
 */
public class AzureSession extends SSLSession<CloudBlobClient> {

    private StorageCredentialsAccountAndKey credentials;

    private final static DisabledX509HostnameVerifier verifier
            = new DisabledX509HostnameVerifier();

    private final static X509TrustManager trust
            = new KeychainX509TrustManager(verifier);

    private final static X509KeyManager key
            = new KeychainX509KeyManager();

    private Preferences preferences
            = Preferences.instance();

    public AzureSession(final Host h) {
        super(h, trust, key);
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        HttpsURLConnection.setFollowRedirects(true);
        HttpsURLConnection.setDefaultSSLSocketFactory(new CustomTrustSSLProtocolSocketFactory(trust, key));
    }

    @Override
    public CloudBlobClient connect(final HostKeyCallback key, final TranscriptListener transcript) throws BackgroundException {
        try {
            credentials = new StorageCredentialsAccountAndKey(host.getCredentials().getUsername(), StringUtils.EMPTY);
            // Client configured with no credentials
            final URI uri = new URI(String.format("%s://%s", Scheme.https, host.getHostname()));
            verifier.setTarget(uri.getHost());
            final CloudBlobClient client = new CloudBlobClient(uri, credentials);
            client.setDirectoryDelimiter(String.valueOf(Path.DELIMITER));
            client.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYFULL);
            final BlobRequestOptions options = client.getDefaultRequestOptions();
            options.setTimeoutIntervalInMs(this.timeout());
            options.setRetryPolicyFactory(new RetryNoRetry());
            return client;
        }
        catch(URISyntaxException e) {
            throw new LoginFailureException(e.getMessage(), e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache, final TranscriptListener transcript) throws BackgroundException {
        // Update credentials
        credentials.setCredentials(new Credentials(
                host.getCredentials().getUsername(), host.getCredentials().getPassword()));
        final Path home = new AzureHomeFinderService(this).find();
        cache.put(home.getReference(), this.list(home, new DisabledListProgressListener()));
    }

    @Override
    protected void logout() throws BackgroundException {
        //
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            if(directory.isRoot()) {
                ResultSegment<CloudBlobContainer> result;
                ResultContinuation token = null;
                final AttributedList<Path> containers = new AttributedList<Path>();
                do {
                    final BlobRequestOptions options = new BlobRequestOptions();
                    options.setRetryPolicyFactory(new RetryNoRetry());
                    result = client.listContainersSegmented(null, ContainerListingDetails.NONE,
                            preferences.getInteger("azure.listing.chunksize"), token,
                            options, null);
                    for(CloudBlobContainer container : result.getResults()) {
                        final PathAttributes attributes = new PathAttributes();
                        attributes.setETag(container.getProperties().getEtag());
                        attributes.setModificationDate(container.getProperties().getLastModified().getTime());
                        containers.add(new Path(String.format("/%s", container.getName()),
                                EnumSet.of(Path.Type.volume, Path.Type.directory), attributes));
                    }
                    listener.chunk(directory, containers);
                    token = result.getContinuationToken();
                }
                while(result.getHasMoreResults());
                return containers;

            }
            else {
                return new AzureObjectListService(this).list(directory, listener);
            }
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Listing directory failed", e, directory);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new AzureReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new AzureWriteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new AzureDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new AzureDeleteFeature(this);
        }
        if(type == Headers.class) {
            return (T) new AzureMetadataFeature(this);
        }
        if(type == Attributes.class) {
            return (T) new AzureAttributesFeature(this);
        }
        if(type == Logging.class) {
            return (T) new AzureLoggingFeature(this);
        }
        if(type == Home.class) {
            return (T) new AzureHomeFinderService(this);
        }
        if(type == Move.class) {
            return (T) new AzureMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new AzureCopyFeature(this);
        }
        if(type == Touch.class) {
            return (T) new AzureTouchFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new AzureUrlProvider(this);
        }
        if(type == AclPermission.class) {
            return (T) new AzureAclPermissionFeature(this);
        }
        return super.getFeature(type);
    }
}
