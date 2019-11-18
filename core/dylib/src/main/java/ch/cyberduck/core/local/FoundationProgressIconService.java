package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSProgress;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferStatus;

import static ch.cyberduck.binding.foundation.NSProgress.*;

public final class FoundationProgressIconService implements IconService {

    @Override
    public boolean set(final Local file, final String image) {
        return false;
    }

    @Override
    public boolean set(final Local file, final TransferStatus status) {
        NSProgress progress = NSProgress.currentProgress();
        if(null == progress) {
            final NSDictionary userInfo = NSDictionary.dictionaryWithObjectsForKeys(
                NSArray.arrayWithObjects(
                    NSString.stringWithString("NSProgressFileOperationKindDownloading"),
                    NSURL.fileURLWithPath(file.getAbsolute())
                ),
                NSArray.arrayWithObjects(NSProgressFileOperationKindKey, NSProgressFileURLKey)
            );
            progress = NSProgress.progressWithParent(null, userInfo);
            progress.setKind(NSProgressKindFile);
            progress.setPausable(false);
            progress.setCancellable(false);
            progress.setCompletedUnitCount(status.getOffset());
            progress.setTotalUnitCount(status.getLength());
            // Sets the receiver as the current progress object of the current thread.
            progress.becomeCurrentWithPendingUnitCount(status.getLength());
            progress.publish();
        }
        else {
            progress.setUserInfoObject_forKey(NSString.stringWithString("NSProgressFileOperationKindDownloading"), NSProgressFileOperationKindKey);
            progress.setUserInfoObject_forKey(NSURL.fileURLWithPath(file.getAbsolute()), NSProgressFileURLKey);
            progress.setCompletedUnitCount(status.getOffset());
            progress.setTotalUnitCount(status.getLength());
        }
        return true;
    }

    @Override
    public boolean remove(final Local file) {
        final NSProgress progress = NSProgress.currentProgress();
        if(null == progress) {
            return false;
        }
        progress.resignCurrent();
        return true;
    }
}
