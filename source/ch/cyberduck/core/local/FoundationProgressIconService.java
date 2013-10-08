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
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class FoundationProgressIconService implements IconService {
    private static final Logger log = Logger.getLogger(FoundationProgressIconService.class);

    private IconService delegate = new WorkspaceIconService();

    public static void register() {
        if(Factory.VERSION_PLATFORM.matches("10\\.8.*")) {
            IconServiceFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
        }
        else {
            log.warn(String.format("Skip registering notifications on %s", Factory.VERSION_PLATFORM));
        }
    }

    private static class Factory extends IconServiceFactory {
        @Override
        protected IconService create() {
            return new FoundationProgressIconService();
        }
    }

    private FoundationProgressIconService() {
        //
    }

    static {
        Native.load("FoundationProgressService");
    }

    @Override
    public boolean set(final Local file, final String image) {
        return delegate.set(file, image);
    }

    @Override
    public boolean set(final Local file, final TransferStatus status) {
        this.setProgress(file.getAbsolute(), status.getCurrent(), status.getLength());
        return delegate.set(file, status);
    }

    @Override
    public boolean remove(final Local file) {
        return delegate.remove(file);
    }

    private native void setProgress(String file, long current, long size);
}