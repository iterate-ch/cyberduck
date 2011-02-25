package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.application.NSAlert;

/**
 * @version $Id$
 */
public interface SheetCallback {

    /**
     * Use default option; 'OK'
     */
    public final int DEFAULT_OPTION = NSAlert.NSAlertDefaultReturn;
    /**
     * Usually cancel option
     */
    public final int OTHER_OPTION = NSAlert.NSAlertOtherReturn;
    /**
     * Non default option
     */
    public final int ALTERNATE_OPTION = NSAlert.NSAlertAlternateReturn;

    /**
     * Called after the sheet has been dismissed by the user. The return codes are defined in
     * <code>ch.cyberduck.ui.cooca.CDSheetCallback</code>
     *
     * @param returncode
     */
    public abstract void callback(final int returncode);

}
