package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.azure.apache.ApacheHttpClient;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccountKind;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.MetadataValidationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import reactor.core.publisher.Mono;

public class AzureSession extends HttpSession<BlobServiceClient> {
    private static final Logger log = LogManager.getLogger(AzureSession.class);

    private final CredentialsHttpPipelinePolicy authenticator = new CredentialsHttpPipelinePolicy();

    public AzureSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public AzureSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    protected BlobServiceClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder pool = builder.build(proxy, this, prompt);
        final BlobServiceClientBuilder builder = new BlobServiceClientBuilder();
        // Pseudo credentials to pass internal validation
        builder.credential(new StorageSharedKeyCredential(StringUtils.EMPTY, StringUtils.EMPTY));
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestIdPolicy());
        policies.add(new RequestRetryPolicy(new RequestRetryOptions()));
        policies.add(new AddDatePolicy());
        policies.add(new AddHeadersPolicy(new com.azure.core.http.HttpHeaders(
                Collections.singletonMap(HttpHeaders.USER_AGENT, new PreferencesUseragentProvider().get()))
        ));
        policies.add(new MetadataValidationPolicy());
        policies.add(authenticator);
        policies.add(new ResponseValidationPolicyBuilder()
                .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID)
                .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
                .build());
        builder.pipeline(new HttpPipelineBuilder()
                .httpClient(new ApacheHttpClient(pool))
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .build());
        builder.endpoint(String.format("%s://%s", Scheme.https, host.getHostname()));
        return builder.buildClient();
    }

    private static final class CredentialsHttpPipelinePolicy implements HttpPipelinePolicy {
        private Credentials credentials = new Credentials();

        public void setCredentials(final Credentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public Mono<HttpResponse> process(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next) {
            if(credentials.isTokenAuthentication()) {
                return new AzureSasCredentialPolicy(new AzureSasCredential(
                        credentials.getToken())).process(context, next);
            }
            return new StorageSharedKeyCredentialPolicy(new StorageSharedKeyCredential(
                    credentials.getUsername(), credentials.getPassword())).process(context, next);
        }
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        // Keep copy of credentials
        authenticator.setCredentials(new Credentials(host.getCredentials()));
        try {
            final AccountKind kind = client.getAccountInfo().getAccountKind();
            if(log.isInfoEnabled()) {
                log.info(String.format("Connected to account of kind %s", kind));
            }
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() {
        //
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new AzureListService(this);
        }
        if(type == Read.class) {
            return (T) new AzureReadFeature(this);
        }
        if(type == Upload.class) {
            return (T) new AzureUploadFeature(this);
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
        if(type == Metadata.class) {
            return (T) new AzureMetadataFeature(this);
        }
        if(type == Find.class) {
            return (T) new AzureFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new AzureAttributesFinderFeature(this);
        }
        if(type == Logging.class) {
            return (T) new AzureLoggingFeature(this);
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
        if(type == Share.class) {
            return (T) new AzureUrlProvider(this);
        }
        if(type == AclPermission.class) {
            return (T) new AzureAclPermissionFeature(this);
        }
        return super._getFeature(type);
    }
}
