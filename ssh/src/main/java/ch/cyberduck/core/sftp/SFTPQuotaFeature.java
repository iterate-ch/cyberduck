package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;

public class SFTPQuotaFeature implements Quota {
    private static final int MIN_SFTP_SPACE_AVAILABLE_VERSION = 6;

    private static final Logger log = LogManager.getLogger(SFTPQuotaFeature.class);

    private final SFTPSession session;

    public SFTPQuotaFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new DefaultHomeFinderService(session).find();
        if(this.isSpaceAvailableExtensionAvailable()) {
            try {
                return this.getSpaceAvailable(session.sftp(), home);
            }
            catch(BackgroundException e) {
                log.info(String.format("Failure obtaining disk quota. %s", e));
            }
        }
        if(this.isStatVFSOpenSSHSupported()) {
            try {
                return this.getSpaceStatVFSOpenSSH(session.sftp(), home);
            }
            catch(BackgroundException e) {
                log.info(String.format("Failure obtaining disk quota. %s", e));
            }
        }
        return unknown;
    }

    private boolean isSpaceAvailableExtensionAvailable() {
        try {
            return session.sftp().getOperativeProtocolVersion() >= MIN_SFTP_SPACE_AVAILABLE_VERSION;
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    private Space getSpaceAvailable(SFTPEngine sftp, final Path directory) throws BackgroundException {
        try {
            final Request request = sftp.newExtendedRequest("space-available").putString(directory.getAbsolute());
            final Response response = sftp.request(request).retrieve();
            switch(response.getType()) {
                case EXTENDED_REPLY:
                    long bytesOnDevice = response.readUInt64();
                    long unusedBytesOnDevice = response.readUInt64();
                    long bytesAvailableToUser = response.readUInt64();
                    long unusedBytesAvailableToUser = response.readUInt64();
                    int bytesPerAllocationUnit = response.readUInt32AsInt();

                    if(bytesAvailableToUser == 0) {
                        if(bytesOnDevice == 0) {
                            throw new IOException("SFTPv6 space-available did not return valid values.");
                        }
                        else {
                            long available = unusedBytesOnDevice;
                            long used = bytesOnDevice - unusedBytesOnDevice;

                            return new Space(used, available);
                        }
                    }
                    else {
                        long available = unusedBytesAvailableToUser;
                        long used = bytesAvailableToUser - unusedBytesAvailableToUser;

                        return new Space(used, available);
                    }

                default:
                    throw new IOException(String.format("Unexpected response type %s", response.getType()));
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", e, directory);
        }
    }

    private boolean isStatVFSOpenSSHSupported() {
        try {
            return session.sftp().supportsServerExtension("statvfs", "openssh.com");
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    private Space getSpaceStatVFSOpenSSH(SFTPEngine sftp, final Path directory) throws BackgroundException {
        try {
            final Request request = sftp.newExtendedRequest("statvfs@openssh.com").putString(directory.getAbsolute());
            final Response response = sftp.request(request).retrieve();
            switch(response.getType()) {
                case EXTENDED_REPLY:
                    long blockSize = response.readUInt64(); /* file system block size */
                    long filesystemBlockSize = response.readUInt64(); /* fundamental fs block size */
                    long totalBlocks = response.readUInt64(); /* number of blocks (unit f_frsize) */
                    long filesystemFreeBlocks = response.readUInt64(); /* free blocks in file system */
                    long blocksAvailable = response.readUInt64(); /* free blocks for non-root */
                    long fileInodes = response.readUInt64(); /* total file inodes */
                    long fileInodesFree = response.readUInt64(); /* free file inodes */
                    long fileInodesAvailable = response.readUInt64(); /* free file inodes for to non-root */
                    byte[] filesystemID = new byte[8]; /* file system id */
                    response.readRawBytes(filesystemID);
                    long flags = response.readUInt64(); /* bit mask of f_flag values */
                    long maximumFilenameLength = response.readUInt64(); /* maximum filename length */

                    long total = totalBlocks * filesystemBlockSize;
                    long available = blocksAvailable * filesystemBlockSize;
                    long used = total - available;

                    return new Space(used, available);
                default:
                    throw new IOException(String.format("Unexpected response type %s", response.getType()));
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", e, directory);
        }
    }
}
