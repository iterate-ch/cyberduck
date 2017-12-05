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

import net.schmizz.sshj.sftp.PacketType;
import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.Response;
import net.schmizz.sshj.sftp.SFTPEngine;

public class SFTPQuotaFeature implements Quota {
    private static final Logger log = Logger.getLogger(SFTPQuotaFeature.class);

    private final SFTPSession session;

    public SFTPQuotaFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new SFTPHomeDirectoryService(session).find();
        Space space = GetSpaceAvailable(session.sftp(), home);

        if(null == space) {
            space = GetSpaceStatVFSOpenSSH(session.sftp(), home);
        }
        if(null == space) {
            space = GetSpaceSSHDF(home);
        }

        return space;
    }

    private Space GetSpaceAvailable(SFTPEngine sftp, Path path) {
        try {
            Request request = sftp.newExtendedRequest("space-available").putString(path.getAbsolute());
            Response response = sftp.request(request).retrieve();
            if(response.getType() == PacketType.STATUS) {
                log.warn(response.readString());
                return null;
            }
            Long bytesOnDevice = response.readUInt64();
            Long unusedBytesOnDevice = response.readUInt64();
            Long bytesAvailableToUser = response.readUInt64();
            Long unusedBytesAvailableToUser = response.readUInt64();
            Integer bytesPerAllocationUnit = response.readUInt32AsInt();

            return new Space(bytesAvailableToUser - unusedBytesAvailableToUser, bytesAvailableToUser);
        }
        catch(IOException ex) {
            return null;
        }
    }

    private Space GetSpaceStatVFSOpenSSH(SFTPEngine sftp, Path path) {
        try {
            Request request = sftp.newExtendedRequest("statvfs@openssh.com").putString(path.getAbsolute());
            Response response = sftp.request(request).retrieve();
            if(response.getType() == PacketType.STATUS) {
                log.warn(response.readString());
                return null;
            }
            Long blockSize = response.readUInt64(); /* file system block size */
            Long filesystemBlockSize = response.readUInt64(); /* fundamental fs block size */
            Long totalBlocks = response.readUInt64(); /* number of blocks (unit f_frsize) */
            Long filesystemFreeBlocks = response.readUInt64(); /* free blocks in file system */
            Long blocksAvailable = response.readUInt64(); /* free blocks for non-root */
            Long fileInodes = response.readUInt64(); /* total file inodes */
            Long fileInodesFree = response.readUInt64(); /* free file inodes */
            Long fileInodesAvailable = response.readUInt64(); /* free file inodes for to non-root */
            Long filesystemID = response.readUInt64(); /* file system id */
            Long flags = response.readUInt64(); /* bit mask of f_flag values */
            Long maximumFilenameLength = response.readUInt64(); /* maximum filename length */

            Long total = totalBlocks * filesystemBlockSize;
            Long available = blocksAvailable * blockSize;
            Long used = total - available;

            return new Space(used, total);
        }
        catch(IOException ex) {
            return null;
        }
    }

    private Space GetSpaceSSHDF(Path path) throws BackgroundException {
        final ThreadLocal<Space> quota = new ThreadLocal<Space>();

        new SFTPCommandFeature(session).send(String.format("df -Pk %s | awk '{print $3, $4}'", path.getAbsolute()), new DisabledProgressListener(),
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
        return quota.get();
    }
}
