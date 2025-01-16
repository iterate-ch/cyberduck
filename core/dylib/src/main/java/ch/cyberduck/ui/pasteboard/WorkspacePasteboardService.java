package ch.cyberduck.ui.pasteboard;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSString;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkspacePasteboardService implements PasteboardService {
    private static final Logger log = LogManager.getLogger(WorkspacePasteboardService.class.getName());

    @Override
    public boolean add(final Type type, final String content) {
        switch(type) {
            case url:
            case string:
                final NSPasteboard pboard = NSPasteboard.generalPasteboard();
                pboard.declareTypes(NSArray.arrayWithObject(NSString.stringWithString(NSPasteboard.StringPboardType)), null);
                if(!pboard.setStringForType(content, NSPasteboard.StringPboardType)) {
                    log.error("Error writing content to {}", NSPasteboard.StringPboardType);
                }
                return true;
            default:
                log.warn("Unsupported pasteboard type {}", type);
                return false;

        }
    }
}
