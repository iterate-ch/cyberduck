package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
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
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.ContainerListingDetails;
import com.microsoft.windowsazure.services.core.storage.AuthenticationScheme;
import com.microsoft.windowsazure.services.core.storage.Credentials;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureSession extends SSLSession<CloudBlobClient> {

    private StorageCredentialsAccountAndKey credentials;

    private final static DisabledX509HostnameVerifier verifier
            = new DisabledX509HostnameVerifier();

    private final static KeychainX509TrustManager trust
            = new KeychainX509TrustManager(verifier);

    public AzureSession(Host h) {
        super(h, trust);
    }

    @Override
    public X509TrustManager getTrustManager() {
        return trust;
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        HttpsURLConnection.setFollowRedirects(true);
        HttpsURLConnection.setDefaultSSLSocketFactory(new CustomTrustSSLProtocolSocketFactory(trust));
    }

    @Override
    public CloudBlobClient connect(final HostKeyCallback key) throws BackgroundException {
        try {
            credentials = new StorageCredentialsAccountAndKey(host.getCredentials().getUsername(), StringUtils.EMPTY);
            // Client configured with no credentials
            final CloudBlobClient client = new CloudBlobClient(new URI(String.format("%s://%s", Scheme.https, host.getHostname())),
                    credentials);
            client.setDirectoryDelimiter(String.valueOf(Path.DELIMITER));
            client.setTimeoutInMs(this.timeout());
            client.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYFULL);

            return client;
        }
        catch(URISyntaxException e) {
            throw new LoginFailureException(e.getMessage(), e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final Cache cache) throws BackgroundException {
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
                            Preferences.instance().getInteger("azure.listing.chunksize"), token,
                            options, null);
                    for(CloudBlobContainer container : result.getResults()) {
                        final PathAttributes attributes = new PathAttributes(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                        attributes.setETag(container.getProperties().getEtag());
                        attributes.setModificationDate(container.getProperties().getLastModified().getTime());
                        containers.add(new Path(String.format("/%s", container.getName()), attributes));
                    }
                    listener.chunk(containers);
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
