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
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferStatus;

public class FinderProgressIconService implements IconService {

    private NSProgress progress;

    @Override
    public boolean set(final Local file, final TransferProgress progress) {
        if(TransferStatus.UNKNOWN_LENGTH == progress.getSize()) {
            return false;
        }
        if(TransferStatus.UNKNOWN_LENGTH == progress.getTransferred()) {
            return false;
        }
        if(null == this.progress) {
            this.progress = NSProgress.progressWithTotalUnitCount(progress.getSize());
            this.progress.setKind(NSProgress.NSProgressKindFile);
            this.progress.setCancellable(false);
            this.progress.setPausable(false);
            this.progress.setFileOperationKind(NSProgress.NSProgressFileOperationKindDownloading);
            this.progress.setFileURL(NSURL.fileURLWithPath(file.getAbsolute()));
            this.progress.publish();
        }
        this.progress.setCompletedUnitCount(progress.getTransferred());
        return true;
    }

    @Override
    public boolean remove(final Local file) {
        if(null == progress) {
            return false;
        }
        progress.unpublish();
        return true;
    }
}
