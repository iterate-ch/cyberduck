package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

public class EueUploadService extends HttpUploadFeature<EueWriteFeature.Chunk> {

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        // Algorithm of the checksum of the chunks. No checksum of the whole file is calculated but verified with the
        // checksum calculated by the server from the checksums of the chunks when completed.
        return ChecksumComputeFactory.get(HashAlgorithm.sha256);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return EnumSet.of(Flags.checksumResponse);
    }
}
