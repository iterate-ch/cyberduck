package ch.cyberduck.ui.cocoa.delegate;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.ui.cocoa.application.NSEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class OpenURLMenuDelegate extends URLMenuDelegate {

    @Override
    protected String getKeyEquivalent() {
        return "b";
    }

    @Override
    protected int getModifierMask() {
        return NSEvent.NSCommandKeyMask | NSEvent.NSShiftKeyMask;
    }

    @Override
    protected abstract List<Path> getSelected();

    @Override
    protected List<DescriptiveUrl> getURLs(Path selected) {
        return new ArrayList<DescriptiveUrl>(selected.getHttpURLs());
    }

    @Override
    public void handle(final List<String> selected) {
        for(String url : selected) {
            BrowserLauncherFactory.get().open(url);
        }
    }
}