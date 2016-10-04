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

import org.apache.log4j.Logger;

public final class PanelReturnCodeMapper {
    private static final Logger log = Logger.getLogger(PanelReturnCodeMapper.class);

    /**
     * Translate return codes from sheet selection
     *
     * @param sender Button pressed
     * @return Sheet callback constant
     * @see SheetCallback#DEFAULT_OPTION
     * @see SheetCallback#CANCEL_OPTION
     */
    public int getOption(final NSButton sender) {
        if(sender.tag() == NSPanel.NSOKButton) {
            return SheetCallback.DEFAULT_OPTION;
        }
        if(sender.tag() == NSPanel.NSCancelButton) {
            return SheetCallback.CANCEL_OPTION;
        }
        if(sender.tag() == NSPanel.NSAlertDefaultReturn) {
            return SheetCallback.DEFAULT_OPTION;
        }
        else if(sender.tag() == NSPanel.NSAlertAlternateReturn) {
            return SheetCallback.ALTERNATE_OPTION;
        }
        else if(sender.tag() == NSPanel.NSAlertOtherReturn) {
            return SheetCallback.CANCEL_OPTION;
        }
        log.warn(String.format("Unknown return code %d", sender.tag()));
        return SheetCallback.DEFAULT_OPTION;
    }

}
