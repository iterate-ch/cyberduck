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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;

import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;

public class SFTPQuotaFeature implements Quota {
    private static final Logger log = Logger.getLogger(SFTPQuotaFeature.class);
    private static final BigInteger threshold = BigInteger.valueOf(Long.MAX_VALUE);

    private final SFTPSession session;

    public SFTPQuotaFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new SFTPHomeDirectoryService(session).find();
        try {
            return this.getSpaceAvailable(session.sftp(), home);
        }
        catch(BackgroundException e) {
            log.info(String.format("Failure obtaining disk quota. %s.", e.getDetail()));
        }
        try {
            return this.getSpaceStatVFSOpenSSH(session.sftp(), home);
        }
        catch(BackgroundException e) {
            log.info(String.format("Failure obtaining disk quota. %s.", e.getDetail()));
        }
        try {
            return this.getSpaceShellPrompt(home);
        }
        catch(BackgroundException e) {
            log.info(String.format("Failure obtaining disk quota. %s.", e.getDetail()));
        }
        return new Space(0L, Long.MAX_VALUE);
    }

    private Space getSpaceAvailable(SFTPEngine sftp, final Path directory) throws BackgroundException {
        try {
            final Request request = sftp.newExtendedRequest("space-available").putString(directory.getAbsolute());
            final Response response = sftp.request(request).retrieve();
            switch(response.getType()) {
                case EXTENDED_REPLY:
                    BigInteger bytesOnDevice = response.readUInt64AsBigInteger();
                    BigInteger unusedBytesOnDevice = response.readUInt64AsBigInteger();
                    BigInteger bytesAvailableToUser = response.readUInt64AsBigInteger();
                    BigInteger unusedBytesAvailableToUser = response.readUInt64AsBigInteger();
                    int bytesPerAllocationUnit = response.readUInt32AsInt();

                    if(BigInteger.ZERO.equals(bytesAvailableToUser)) {
                        if(BigInteger.ZERO.equals(bytesOnDevice)) {
                            throw new IOException("SFTPv6 space-available did not return valid values.");
                        }
                        else {
                            BigInteger available = unusedBytesOnDevice;
                            BigInteger used = bytesOnDevice.subtract(unusedBytesOnDevice);

                            if(threshold.compareTo(available) < 0 || threshold.compareTo(used) < 0) {
                                throw new IOException(String.format("Available %s or used %s exceed threshold of %s", available, used, threshold));
                            }
                            return new Space(used.longValue(), available.longValue());
                        }
                    }
                    else {
                        BigInteger available = unusedBytesAvailableToUser;
                        BigInteger used = bytesAvailableToUser.subtract(unusedBytesAvailableToUser);

                        if(threshold.compareTo(available) < 0 || threshold.compareTo(used) < 0) {
                            throw new IOException(String.format("Available %s or used %s exceed threshold of %s", available, used, threshold));
                        }
                        return new Space(used.longValue(), available.longValue());
                    }

                default:
                    throw new IOException(String.format("Unexpected response type %s", response.getType()));
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", e, directory);
        }
    }

    private Space getSpaceStatVFSOpenSSH(SFTPEngine sftp, final Path directory) throws BackgroundException {
        try {
            final Request request = sftp.newExtendedRequest("statvfs@openssh.com").putString(directory.getAbsolute());
            final Response response = sftp.request(request).retrieve();
            switch(response.getType()) {
                case EXTENDED_REPLY:
                    BigInteger blockSize = response.readUInt64AsBigInteger(); /* file system block size */
                    BigInteger filesystemBlockSize = response.readUInt64AsBigInteger(); /* fundamental fs block size */
                    BigInteger totalBlocks = response.readUInt64AsBigInteger(); /* number of blocks (unit f_frsize) */
                    BigInteger filesystemFreeBlocks = response.readUInt64AsBigInteger(); /* free blocks in file system */
                    BigInteger blocksAvailable = response.readUInt64AsBigInteger(); /* free blocks for non-root */
                    BigInteger fileInodes = response.readUInt64AsBigInteger(); /* total file inodes */
                    BigInteger fileInodesFree = response.readUInt64AsBigInteger(); /* free file inodes */
                    BigInteger fileInodesAvailable = response.readUInt64AsBigInteger(); /* free file inodes for to non-root */
                    BigInteger filesystemID = response.readUInt64AsBigInteger(); /* file system id */
                    BigInteger flags = response.readUInt64AsBigInteger(); /* bit mask of f_flag values */
                    BigInteger maximumFilenameLength = response.readUInt64AsBigInteger(); /* maximum filename length */

                    BigInteger total = totalBlocks.multiply(filesystemBlockSize);
                    BigInteger available = blocksAvailable.multiply(blockSize);
                    BigInteger used = total.subtract(available);

                    BigInteger threshold = BigInteger.valueOf(Long.MAX_VALUE);

                    if(threshold.compareTo(available) < 0 || threshold.compareTo(used) < 0) {
                        throw new IOException(String.format("Available %s or used %s exceed threshold of %s", available, used, threshold));
                    }
                    return new Space(used.longValue(), available.longValue());
                default:
                    throw new IOException(String.format("Unexpected response type %s", response.getType()));
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", e, directory);
        }
    }

    private Space getSpaceShellPrompt(final Path directory) throws BackgroundException {
        final ThreadLocal<Space> quota = new ThreadLocal<Space>();
        new SFTPCommandFeature(session).send(String.format("df -Pk %s | awk '{print $3, $4}'", directory.getAbsolute()), new DisabledProgressListener(),
            new TranscriptListener() {
                @Override
                public void log(final Type request, final String output) {
                    switch(request) {
                        case response:
                            final String[] numbers = StringUtils.split(output, ' ');
                            if(numbers.length == 2) {
                                try {
                                    Long used = Long.valueOf(numbers[0]) * 1000L;
                                    Long available = Long.valueOf(numbers[1]) * 1000L;
                                    quota.set(new Space(used, available));
                                }
                                catch(NumberFormatException e) {
                                    log.warn(String.format("Ignore line %s", output));
                                }
                            }
                            else {
                                log.warn(String.format("Ignore line %s", output));
                            }
                    }
                }
            });
        if(null == quota.get()) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", new IOException(), directory);
        }
        return quota.get();
    }
}
