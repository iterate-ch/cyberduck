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

import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;

/**
 * Blob Storage and Data Lake Storage clients sharing the same HTTP pipeline
 */
public final class AzureClient {

    private final BlobServiceClient blob;
    private final DataLakeServiceClient datalake;

    public AzureClient(final BlobServiceClient blob, final DataLakeServiceClient datalake) {
        this.blob = blob;
        this.datalake = datalake;
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
