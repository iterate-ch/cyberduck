package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.NotfoundException;

/**
 * @version $Id$
 */
public class BundleApplicationResourcesFinder implements ApplicationResourcesFinder {

    @Override
    public Local find() {
        final NSBundle main = NSBundle.mainBundle();
        final Local executable = LocalFactory.get(main.executablePath());
        if(executable.isSymbolicLink()) {
            final Local target;
            try {
                target = LocalFactory.get(main.executablePath()).getSymlinkTarget();
            }
            catch(NotfoundException e) {
                return LocalFactory.get(main.resourcePath());
            }
            Local folder = target.getParent();
            NSBundle bundle;
            do {
                bundle = NSBundle.bundleWithPath(folder.getAbsolute());
                folder = folder.getParent();
            }
            while(bundle.executablePath() == null);
            return LocalFactory.get(bundle.resourcePath());
        }
        return LocalFactory.get(main.resourcePath());
    }
}
