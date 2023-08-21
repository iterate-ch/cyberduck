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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Quota;

import com.hierynomus.msfscc.fileinformation.ShareInfo;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBQuotaFeature implements Quota {

    private final SMBSession session;

    public SMBQuotaFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final ShareInfo info = session.share.getShareInformation();
            return new Space(info.getTotalSpace() - info.getFreeSpace(), info.getFreeSpace());
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Failure to read attributes of {0}", e, Home.ROOT);
        }
    }
}
