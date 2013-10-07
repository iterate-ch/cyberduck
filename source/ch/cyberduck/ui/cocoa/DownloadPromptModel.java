package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.resources.IconCacheFactory;

/**
 * @version $Id$
 */
public class DownloadPromptModel extends TransferPromptModel {

    public DownloadPromptModel(final TransferPromptController c, final Transfer transfer, final Cache cache) {
        super(c, transfer, cache);
    }

    @Override
    protected NSObject objectValueForItem(final Path file, final String identifier) {
        if(identifier.equals(Column.size.name())) {
            return NSAttributedString.attributedStringWithAttributes(
                    SizeFormatterFactory.get().format(file.getLocal().attributes().getSize()),
                    TableCellAttributes.browserFontRightAlignment());
        }
        if(identifier.equals(Column.warning.name())) {
            if(file.attributes().isFile()) {
                if(file.attributes().getSize() == 0) {
                    return IconCacheFactory.<NSImage>get().iconNamed("alert.tiff");
                }
                if(status.containsKey(file)) {
                    if(file.getLocal().attributes().getSize() > status.get(file).getLength()) {
                        return IconCacheFactory.<NSImage>get().iconNamed("alert.tiff");
                    }
                }
            }
            return null;
        }
        return super.objectValueForItem(file, identifier);
    }
}