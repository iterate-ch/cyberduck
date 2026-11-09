package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;

/**
 * Blob Storage and Data Lake Storage clients sharing the same HTTP client
 */
public final class AzureClient {

    private final BlobServiceClientBuilder blobBuilder;
    private final DataLakeServiceClientBuilder datalakeBuilder;

    private volatile BlobServiceClient blob;
    private volatile DataLakeServiceClient datalake;

    public AzureClient(final BlobServiceClientBuilder blobBuilder, final DataLakeServiceClientBuilder datalakeBuilder) {
        this.blobBuilder = blobBuilder;
        this.datalakeBuilder = datalakeBuilder;
        this.blob = blobBuilder.buildClient();
    }

    /**
     * Rebuild the clients for the credentials. The credential is also retained by the Data Lake client to compose
     * the source of a rename.
     */
    public void setCredentials(final Credentials credentials) {
        if(credentials.isTokenAuthentication()) {
            final AzureSasCredential sas = new AzureSasCredential(credentials.getToken());
            blobBuilder.credential(sas);
            datalakeBuilder.credential(sas);
        }
        else {
            final StorageSharedKeyCredential key = new StorageSharedKeyCredential(credentials.getUsername(), credentials.getPassword());
            blobBuilder.credential(key);
            datalakeBuilder.credential(key);
        }
        this.build();
    }

    /**
     * Rebuild the clients to authorize requests with a bearer token such as from Microsoft Entra ID
     */
    public void setCredentials(final TokenCredential token) {
        blobBuilder.credential(token);
        datalakeBuilder.credential(token);
        this.build();
    }

    private void build() {
        blob = blobBuilder.buildClient();
        datalake = datalakeBuilder.buildClient();
    }

    public BlobServiceClient getBlobServiceClient() {
        return blob;
    }

    /**
     * @return Client for operations on hierarchical namespaces
     */
    public DataLakeServiceClient getDataLakeServiceClient() {
        return datalake;
    }

    public HttpPipeline getHttpPipeline() {
        return blob.getHttpPipeline();
    }
}
