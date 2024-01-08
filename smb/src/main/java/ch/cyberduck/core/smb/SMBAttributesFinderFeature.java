package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Quota;

import java.io.IOException;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.ShareInfo;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;

public class SMBAttributesFinderFeature implements AttributesFinder, AttributesAdapter<FileAllInformation> {

    private final SMBSession session;

    public SMBAttributesFinderFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try (final DiskShare share = session.openShare(file)) {
            if(new SMBPathContainerService(session).isContainer(file)) {
                final ShareInfo shareInformation = share.getShareInformation();
                final PathAttributes attributes = new PathAttributes();
                final long used = shareInformation.getTotalSpace() - shareInformation.getFreeSpace();
                attributes.setSize(used);
                attributes.setQuota(new Quota.Space(used, shareInformation.getFreeSpace()));
                return attributes;
            }
            else {
                final FileAllInformation fileInformation = share.getFileInformation(new SMBPathContainerService(session).getKey(file));
                if(file.isDirectory() && !fileInformation.getStandardInformation().isDirectory()) {
                    throw new NotfoundException(String.format("File %s found but type is not directory", file.getName()));
                }
                else if(file.isFile() && fileInformation.getStandardInformation().isDirectory()) {
                    throw new NotfoundException(String.format("File %s found but type is not file", file.getName()));
                }
                return this.toAttributes(fileInformation);
            }
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new SMBTransportExceptionMappingService().map("Cannot read container configuration", e);
        }
        finally {
            session.releaseShare(file);
        }
    }

    @Override
    public PathAttributes toAttributes(final FileAllInformation model) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setAccessedDate(model.getBasicInformation().getLastAccessTime().toEpochMillis());
        attributes.setModificationDate(model.getBasicInformation().getLastWriteTime().toEpochMillis());
        attributes.setCreationDate(model.getBasicInformation().getCreationTime().toEpochMillis());
        if(!model.getStandardInformation().isDirectory()) {
            attributes.setSize(model.getStandardInformation().getEndOfFile());
        }
        return attributes;
    }
}
