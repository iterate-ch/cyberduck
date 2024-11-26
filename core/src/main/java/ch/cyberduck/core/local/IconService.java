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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferProgress;

public interface IconService {

    Icon disabled = new Icon() {
        @Override
        public boolean update(final TransferProgress progress) {
            return false;
        }

        @Override
        public boolean remove() {
            return false;
        }
    };

    /**
     * Get icon updater to track progress
     *
     * @param file Local file
     * @return Updater to send continious progress updates to
     */
    Icon get(Transfer.Type type, Local file);

    interface Icon {
        /**
         * @param progress Transfer status with transferred bytes set in offset
         * @return True if icon is set
         */
        boolean update(TransferProgress progress);

        /**
         * Remove custom icon
         *
         * @return True if icon is set
         */
        boolean remove();
    }
}
