package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AlertSheetReturnCodeMapper {
    private static final Logger log = LogManager.getLogger(AlertSheetReturnCodeMapper.class);

    /**
     * Translate return codes from sheet selection
     *
     * @param sender Button pressed
     * @return Sheet callback constant
     * @see SheetCallback#DEFAULT_OPTION
     * @see SheetCallback#CANCEL_OPTION
     */
    public int getOption(final NSButton sender) {
        return this.getOption(sender.tag());
    }

    public int getOption(final int option) {
        log.debug("Map selected tag {}", option);
        switch(option) {
            case NSAlert.NSAlertFirstButtonReturn:
            case NSPanel.NSOKButton:
                return SheetCallback.DEFAULT_OPTION;
            case NSAlert.NSAlertSecondButtonReturn:
            case NSPanel.NSCancelButton:
                return SheetCallback.CANCEL_OPTION;
            case NSAlert.NSAlertThirdButtonReturn:
                return SheetCallback.ALTERNATE_OPTION;
        }
        log.warn("Unknown return code {}", option);
        return SheetCallback.DEFAULT_OPTION;
    }

}
