package ch.cyberduck.core.updater;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import java.util.Objects;

public interface UpdateChecker {
    /**
     * Check for new version
     *
     * @param background Run in background
     */
    void check(boolean background);

    /**
     * @return True if user has privilege to update installed application
     */
    boolean hasUpdatePrivileges();

    /**
     * @return True if user interface to check for update should be enabled
     */
    boolean isUpdateInProgress();

    void addHandler(Handler handler);

    /**
     * Receive notification for update found to allow display of custom user interface.
     */
    interface Handler {
        /**
         * Handle update found
         *
         * @param item Update description
         * @return True if standard driver should not continue handling update or false if no additinal prompt for update is shown
         */
        boolean handle(UpdateChecker.Update item);
    }

    final class Update {
        private final String revision;
        private final String displayVersionString;


        public Update(final String revision, final String displayVersionString) {
            this.revision = revision;
            this.displayVersionString = displayVersionString;
        }

        public String getRevision() {
            return revision;
        }

        public String getDisplayVersionString() {
            return displayVersionString;
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            final Update version = (Update) o;
            return Objects.equals(revision, version.revision);
        }

        @Override
        public int hashCode() {
            return Objects.hash(revision);
        }
    }
}
