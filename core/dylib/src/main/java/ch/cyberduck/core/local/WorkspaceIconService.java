package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.unicode.NFDNormalizer;

import org.rococoa.cocoa.foundation.NSUInteger;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class WorkspaceIconService implements IconService {

    private static final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    @Override
    public Icon get(final Transfer.Type type, final Local file) {
        switch(type) {
            case download:
                return new Icon() {
                    // An integer between 0 and 9
                    private int step = 0;

                    @Override
                    public boolean update(final TransferProgress progress) {
                        if(progress.getSize() > PreferencesFactory.get().getLong("queue.download.icon.threshold")) {
                            final int fraction = new BigDecimal(progress.getTransferred()).divide(new BigDecimal(progress.getSize()), 1, RoundingMode.DOWN).multiply(BigDecimal.TEN).intValue();
                            if(fraction >= step) {
                                // Another 10 percent of the file has been transferred
                                return WorkspaceIconService.update(file, IconCacheFactory.<NSImage>get().iconNamed(String.format("download%d.icns", step = fraction)));
                            }
                            return false;
                        }
                        return false;
                    }

                    @Override
                    public boolean remove() {
                        // The Finder will display the default icon for this file type
                        return WorkspaceIconService.update(file, null);
                    }
                };
        }
        return disabled;
    }

    public static boolean update(final Local file, final NSImage icon) {
        synchronized(NSWorkspace.class) {
            // Specify 0 if you want to generate icons in all available icon representation formats
            if(workspace.setIcon_forFile_options(icon, file.getAbsolute(), new NSUInteger(0))) {
                workspace.noteFileSystemChanged(new NFDNormalizer().normalize(file.getAbsolute()).toString());
                return true;
            }
            return false;
        }
    }
}
