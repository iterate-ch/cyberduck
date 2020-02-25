package ch.cyberduck.ui.quicklook;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;

import java.util.List;

public interface QuickLook {

    /**
     * Does not open QuickLook panel but only changes the selection.
     *
     * @param files Display these files in QuickLook panel
     */
    void select(List<Local> files);

    /**
     * @return QuickLook panel is visible
     */
    boolean isOpen();

    /**
     * Open QuickLook panel
     */
    void open();

    /**
     * Close QuickLook panel if any
     */
    void close();
}
