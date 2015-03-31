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
import ch.cyberduck.core.PreferencesUseragentProvider;
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
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

import com.microsoft.azure.storage.Credentials;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;

/**
 * @version $Id$
 */
public class AzureSession extends SSLSession<CloudBlobClient> {

    private StorageCredentialsAccountAndKey credentials;

    private OperationContext context
            = new OperationContext();

    private StorageEvent<SendingRequestEvent> listener;

    public AzureSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public AzureSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    static {
        HttpsURLConnection.setFollowRedirects(true);
    }

    @Override
    public CloudBlobClient connect(final HostKeyCallback callback) throws BackgroundException {
        try {
            credentials = new StorageCredentialsAccountAndKey(host.getCredentials().getUsername(), StringUtils.EMPTY);
            // Client configured with no credentials
            final URI uri = new URI(String.format("%s://%s", Scheme.https, host.getHostname()));
            final CloudBlobClient client = new CloudBlobClient(uri, credentials);
            client.setDirectoryDelimiter(String.valueOf(Path.DELIMITER));
            final BlobRequestOptions options = client.getDefaultRequestOptions();
            options.setTimeoutIntervalInMs(this.timeout());
            options.setRetryPolicyFactory(new RetryNoRetry());
            context.setUserHeaders(new HashMap<String, String>(Collections.singletonMap(
                            HttpHeaders.USER_AGENT, new PreferencesUseragentProvider().get()))
            );
            context.getSendingRequestEventHandler().addListener(listener = new StorageEvent<SendingRequestEvent>() {
                @Override
                public void eventOccurred(final SendingRequestEvent event) {
                    if(event.getConnectionObject() instanceof HttpsURLConnection) {
                        final HttpsURLConnection connection = (HttpsURLConnection) event.getConnectionObject();
                        connection.setSSLSocketFactory(new CustomTrustSSLProtocolSocketFactory(trust, key));
                        connection.setHostnameVerifier(new DisabledX509HostnameVerifier());
                    }
                }
            });
            return client;
        }
        catch(URISyntaxException e) {
            throw new LoginFailureException(e.getMessage(), e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        // Update credentials
        credentials.setCredentials(new Credentials(
                host.getCredentials().getUsername(), host.getCredentials().getPassword()));
        final Path home = new AzureHomeFinderService(this).find();
        cache.put(home, this.list(home, new DisabledListProgressListener()));
    }

    @Override
    protected void disconnect() {
        context.getSendingRequestEventHandler().removeListener(listener);
        super.disconnect();
    }

    @Override
    protected void logout() throws BackgroundException {
        //
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return new AttributedList<Path>(new AzureContainerListService(this, context).list(listener));
        }
        else {
            return new AzureObjectListService(this, context).list(directory, listener);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new AzureReadFeature(this, context);
        }
        if(type == Write.class) {
            return (T) new AzureWriteFeature(this, context);
        }
        if(type == Directory.class) {
            return (T) new AzureDirectoryFeature(this, context);
        }
        if(type == Delete.class) {
            return (T) new AzureDeleteFeature(this, context);
        }
        if(type == Headers.class) {
            return (T) new AzureMetadataFeature(this, context);
        }
        if(type == Attributes.class) {
            return (T) new AzureAttributesFeature(this, context);
        }
        if(type == Logging.class) {
            return (T) new AzureLoggingFeature(this, context);
        }
        if(type == Home.class) {
            return (T) new AzureHomeFinderService(this);
        }
        if(type == Move.class) {
            return (T) new AzureMoveFeature(this, context);
        }
        if(type == Copy.class) {
            return (T) new AzureCopyFeature(this, context);
        }
        if(type == Touch.class) {
            return (T) new AzureTouchFeature(this, context);
        }
        if(type == UrlProvider.class) {
            return (T) new AzureUrlProvider(this);
        }
        if(type == AclPermission.class) {
            return (T) new AzureAclPermissionFeature(this, context);
        }
        return super.getFeature(type);
    }
}
