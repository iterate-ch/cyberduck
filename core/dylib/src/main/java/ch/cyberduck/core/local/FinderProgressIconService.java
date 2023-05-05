package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSProgress;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferStatus;

public class FinderProgressIconService implements IconService {

    @Override
    public boolean set(final Local file, final TransferProgress status) {
        NSProgress progress = NSProgress.currentProgress();
        if(null == progress) {
            progress = NSProgress.progressWithTotalUnitCount(status.getSize());
            progress.setKind(NSProgress.NSProgressKindFile);
            progress.setCancellable(false);
            progress.setPausable(false);
            progress.setUserInfoObject_forKey(NSString.stringWithString(NSProgress.NSProgressFileOperationKindDownloading), NSProgress.NSProgressFileOperationKindKey);
            progress.setUserInfoObject_forKey(NSURL.fileURLWithPath(file.getAbsolute()), NSProgress.NSProgressFileURLKey);
            progress.publish();
            progress.becomeCurrentWithPendingUnitCount(status.getSize() - status.getTransferred());
        }
        if(TransferStatus.UNKNOWN_LENGTH == status.getSize()) {
            return false;
        }
        if(TransferStatus.UNKNOWN_LENGTH == status.getTransferred()) {
            return false;
        }
        progress.setTotalUnitCount(status.getSize());
        progress.setCompletedUnitCount(status.getTransferred());
        return true;
    }

    @Override
    public boolean remove(final Local file) {
        final NSProgress progress = NSProgress.currentProgress();
        if(null == progress) {
            return false;
        }
        progress.resignCurrent();
        progress.unpublish();
        return true;
    }
}
