package ch.cyberduck.core.transfer;

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

public final class TransferOptions {

    /**
     * Resume requested using user interface
     */
    public boolean resumeRequested = false;

    /**
     * Reload requested using user interface
     */
    public boolean reloadRequested = false;

    public TransferOptions reload(boolean e) {
        reloadRequested = e;
        return this;
    }

    public TransferOptions resume(boolean e) {
        resumeRequested = e;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferOptions{");
        sb.append("resumeRequested=").append(resumeRequested);
        sb.append(", reloadRequested=").append(reloadRequested);
        sb.append('}');
        return sb.toString();
    }
}
