package ch.cyberduck.ui.cocoa.quicklook;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Local;

/**
 * @version $Id$
 */
public interface QuickLookInterface {

    /**
     * Does not open QuickLook panel but only changes the selection.
     * @param files Display these files in QuickLook panel
     */
    public void select(Collection<Local> files);

    /**
     *
     * @return QuickLook implementation found.
     */
    public boolean isAvailable();

    /**
     *
     * @return QuickLook panel is visible
     */
    public boolean isOpen();

    public void willBeginQuickLook();

    /**
     * Open QuickLook panel
     */
    public void open();

    /**
     * Close QuickLook panel if any
     */
    public void close();

    public void didEndQuickLook();
}
